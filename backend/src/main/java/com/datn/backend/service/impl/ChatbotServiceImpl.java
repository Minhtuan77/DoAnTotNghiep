package com.datn.backend.service.impl;

import com.datn.backend.dto.request.ChatRequest;
import com.datn.backend.dto.response.*;
import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.*;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.*;
import com.datn.backend.service.ChatbotService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.llm.base-url:}") private String llmBaseUrl;
    @Value("${ai.llm.api-key:}") private String llmApiKey;
    @Value("${ai.llm.model:gpt-4.1-mini}") private String llmModel;

    @Override
    @Transactional
    public ChatResponse send(Long userId, ChatRequest request) {
        String sessionId = normalizeSession(request.getSessionId());
        ChatConversation conversation = resolveConversation(userId, sessionId, request.getConversationId());
        messageRepository.save(ChatMessage.builder().conversation(conversation).senderType(ChatSenderType.CUSTOMER).messageText(request.getMessage().trim()).build());

        String context = buildContext(userId, request.getMessage());
        List<ChatMessage> history = messageRepository.findTop30ByConversation_ConversationIdOrderByCreatedAtDesc(conversation.getConversationId());
        Collections.reverse(history);
        String answer = callLlm(request.getMessage(), context, history);
        if (answer == null || answer.isBlank()) answer = localFallback(request.getMessage(), context, userId != null);
        boolean escalate = answer.startsWith("NEED_HUMAN:");
        if (escalate) { answer = answer.substring("NEED_HUMAN:".length()).trim(); conversation.setStatus(ChatConversationStatus.ESCALATED); conversationRepository.save(conversation); }
        ChatMessage reply = messageRepository.save(ChatMessage.builder().conversation(conversation).senderType(ChatSenderType.BOT).messageText(answer).build());
        return ChatResponse.builder().conversationId(conversation.getConversationId()).status(conversation.getStatus()).reply(toMessage(reply)).build();
    }

    private ChatConversation resolveConversation(Long userId, String sessionId, Long id) {
        if (id != null) {
            Optional<ChatConversation> found = userId != null ? conversationRepository.findByConversationIdAndUser_UserId(id,userId) : conversationRepository.findByConversationIdAndSessionId(id,sessionId);
            ChatConversation c=found.orElseThrow(()->new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
            if(c.getStatus()==ChatConversationStatus.CLOSED) throw new IllegalArgumentException("Cuộc trò chuyện đã đóng");
            return c;
        }
        User user=userId==null?null:userRepository.findById(userId).orElse(null);
        return conversationRepository.save(ChatConversation.builder().user(user).sessionId(sessionId).status(ChatConversationStatus.OPEN).build());
    }

    private String buildContext(Long userId, String message) {
        String lower=message.toLowerCase(Locale.ROOT);
        List<Product> active=productRepository.findByStatus(ProductStatus.ACTIVE);
        List<Product> relevant=active.stream().map(p->Map.entry(p, relevance(p,lower))).filter(e->e.getValue()>0).sorted(Map.Entry.<Product,Integer>comparingByValue().reversed()).limit(8).map(Map.Entry::getKey).toList();
        if(relevant.isEmpty()) relevant=active.stream().sorted(Comparator.comparingInt((Product p)->Optional.ofNullable(p.getSoldCount()).orElse(0)).reversed()).limit(6).toList();
        String products=relevant.stream().map(p->{Inventory i=inventoryRepository.findByProduct_ProductId(p.getProductId()).orElse(null);int stock=i==null?0:Math.max(0,i.getQuantityOnHand()-i.getQuantityReserved());return String.format("- ID %d | %s | giá %s | sale %s | tồn %d | danh mục %s | đánh giá %s",p.getProductId(),p.getName(),p.getPrice(),p.getSalePrice(),stock,p.getCategory()==null?"":p.getCategory().getName(),p.getAvgRating());}).collect(Collectors.joining("\n"));
        String orders="Khách chưa đăng nhập: không được tra cứu đơn hàng.";
        if(userId!=null){orders=orderRepository.findTop10ByUser_UserIdOrderByCreatedAtDesc(userId).stream().map(o->String.format("- %s | %s | tổng %s | ngày %s",o.getOrderCode(),o.getStatus(),o.getTotalAmount(),o.getCreatedAt())).collect(Collectors.joining("\n"));if(orders.isBlank())orders="Người dùng chưa có đơn hàng.";}
        return "DỮ LIỆU SẢN PHẨM THỰC TẾ:\n"+products+"\n\nĐƠN HÀNG CỦA CHÍNH NGƯỜI DÙNG (không có địa chỉ/sđt):\n"+orders+"\n\nCHÍNH SÁCH ĐÃ CÓ TRONG HỆ THỐNG:\n- Thanh toán: COD hoặc VNPAY.\n- Voucher phải còn hiệu lực, trong thời gian dùng, chưa vượt giới hạn và đơn đạt giá trị tối thiểu; giảm không vượt điều kiện voucher.\n- Khách chỉ hủy đơn khi đang chờ thanh toán/chờ xác nhận.\n- Bảo hành/đổi trả chi tiết chưa được cấu hình thành dữ liệu chính sách; nếu được hỏi, phải nói chưa đủ dữ liệu và hướng tới nhân viên hỗ trợ.";
    }
    private int relevance(Product p,String q){int s=0;for(String t:q.split("\\s+")){if(t.length()<3)continue;if(p.getName()!=null&&p.getName().toLowerCase(Locale.ROOT).contains(t))s+=4;if(p.getDescription()!=null&&p.getDescription().toLowerCase(Locale.ROOT).contains(t))s++;if(p.getCategory()!=null&&p.getCategory().getName().toLowerCase(Locale.ROOT).contains(t))s+=2;if(p.getBrand()!=null&&p.getBrand().getName().toLowerCase(Locale.ROOT).contains(t))s+=2;}return s;}

    private String callLlm(String question,String context,List<ChatMessage> history){
        if(llmBaseUrl==null||llmBaseUrl.isBlank()||llmApiKey==null||llmApiKey.isBlank())return null;
        try{
            String url=llmBaseUrl.endsWith("/chat/completions")?llmBaseUrl:llmBaseUrl.replaceAll("/$","")+"/chat/completions";
            List<Map<String,String>> messages=new ArrayList<>();
            messages.add(Map.of("role","system","content","Bạn là chatbot CSKH cho website Nhà Tiện Nghi. Chỉ dùng dữ liệu được cung cấp. Không bịa giá/tồn kho/chính sách. Không tiết lộ dữ liệu cá nhân. Chỉ tra cứu đơn của chính user đã xác thực. Nếu thiếu dữ liệu, bắt đầu câu trả lời bằng NEED_HUMAN: và hướng khách tới nhân viên. Trả lời tiếng Việt ngắn gọn, hữu ích.\n\n"+context));
            for(ChatMessage m:history.stream().skip(Math.max(0,history.size()-10)).toList()) messages.add(Map.of("role",m.getSenderType()==ChatSenderType.CUSTOMER?"user":"assistant","content",m.getMessageText()));
            Map<String,Object> body=new LinkedHashMap<>();body.put("model",llmModel);body.put("messages",messages);body.put("temperature",0.2);body.put("max_tokens",500);
            String raw=RestClient.create().post().uri(url).header(HttpHeaders.AUTHORIZATION,"Bearer "+llmApiKey).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(String.class);
            JsonNode n=objectMapper.readTree(raw);return n.path("choices").path(0).path("message").path("content").asText(null);
        }catch(Exception e){return null;}
    }
    private String localFallback(String question,String context,boolean loggedIn){
        String q=question.toLowerCase(Locale.ROOT);
        if(q.contains("đơn")||q.contains("trạng thái")){if(!loggedIn)return "Bạn cần đăng nhập để mình có thể tra cứu đơn hàng của chính bạn.";return "Mình đã lấy dữ liệu đơn hàng của tài khoản hiện tại. "+extractSection(context,"ĐƠN HÀNG", "CHÍNH SÁCH");}
        if(q.contains("giá")||q.contains("còn hàng")||q.contains("sản phẩm")||q.contains("mua")||q.contains("nồi")||q.contains("bếp"))return "Dựa trên catalog hiện tại, đây là các sản phẩm gần với yêu cầu của bạn:\n"+extractSection(context,"DỮ LIỆU SẢN PHẨM", "ĐƠN HÀNG");
        if(q.contains("voucher")||q.contains("thanh toán"))return "Hệ thống hiện hỗ trợ COD và VNPAY. Voucher chỉ áp dụng khi còn hiệu lực, đúng thời gian, chưa vượt giới hạn sử dụng và đơn hàng đạt giá trị tối thiểu theo voucher.";
        if(q.contains("bảo hành")||q.contains("đổi trả")||q.contains("chính sách"))return "NEED_HUMAN: Hiện dữ liệu chi tiết về bảo hành/đổi trả chưa được cấu hình đầy đủ trong hệ thống. Bạn nên liên hệ nhân viên hỗ trợ để nhận thông tin chính xác.";
        return "NEED_HUMAN: Mình chưa có đủ dữ liệu trong hệ thống để trả lời chắc chắn câu hỏi này. Bạn có thể hỏi về sản phẩm, giá, tồn kho, đơn hàng, thanh toán hoặc voucher; nếu cần mình sẽ chuyển hướng tới nhân viên hỗ trợ.";
    }
    private String extractSection(String text,String start,String end){int a=text.indexOf(start),b=text.indexOf(end,a+1);if(a<0)return "";String s=text.substring(a,b>0?b:text.length());int colon=s.indexOf(':');return colon>=0?s.substring(colon+1).trim():s;}

    @Override @Transactional(readOnly=true)
    public ChatConversationResponse getConversation(Long userId,String sessionId,Long id){ChatConversation c=userId!=null?conversationRepository.findByConversationIdAndUser_UserId(id,userId).orElseThrow(()->new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện")):conversationRepository.findByConversationIdAndSessionId(id,normalizeSession(sessionId)).orElseThrow(()->new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));return toConversation(c,true);}
    @Override @Transactional(readOnly=true)
    public List<ChatConversationResponse> list(Long userId,String sessionId){List<ChatConversation> list=userId!=null?conversationRepository.findTop20ByUser_UserIdOrderByCreatedAtDesc(userId):conversationRepository.findTop20BySessionIdOrderByCreatedAtDesc(normalizeSession(sessionId));return list.stream().map(c->toConversation(c,false)).toList();}
    @Override @Transactional
    public ChatConversationResponse close(Long userId,String sessionId,Long id){ChatConversation c=userId!=null?conversationRepository.findByConversationIdAndUser_UserId(id,userId).orElseThrow(()->new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện")):conversationRepository.findByConversationIdAndSessionId(id,normalizeSession(sessionId)).orElseThrow(()->new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));c.setStatus(ChatConversationStatus.CLOSED);c.setClosedAt(LocalDateTime.now());conversationRepository.save(c);return toConversation(c,true);}
    private ChatConversationResponse toConversation(ChatConversation c,boolean messages){return ChatConversationResponse.builder().conversationId(c.getConversationId()).status(c.getStatus()).createdAt(c.getCreatedAt()).closedAt(c.getClosedAt()).messages(messages?messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(c.getConversationId()).stream().map(this::toMessage).toList():List.of()).build();}
    private ChatMessageResponse toMessage(ChatMessage m){return ChatMessageResponse.builder().messageId(m.getMessageId()).senderType(m.getSenderType()).messageText(m.getMessageText()).referencedEntityType(m.getReferencedEntityType()).referencedEntityId(m.getReferencedEntityId()).createdAt(m.getCreatedAt()).build();}
    private String normalizeSession(String s){if(s==null||s.isBlank())return "guest-"+UUID.randomUUID();String v=s.trim();return v.length()>100?v.substring(0,100):v;}
}
