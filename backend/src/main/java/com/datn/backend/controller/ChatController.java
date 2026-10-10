package com.datn.backend.controller;

import com.datn.backend.dto.request.ChatRequest;
import com.datn.backend.dto.response.*;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatbotService service;
    @PostMapping("/messages")
    public ApiResponse<ChatResponse> send(@AuthenticationPrincipal CustomUserDetails user,@Valid @RequestBody ChatRequest request){return ApiResponse.success("Phản hồi chatbot",service.send(user==null?null:user.getUserId(),request));}
    @GetMapping("/conversations")
    public ApiResponse<List<ChatConversationResponse>> list(@AuthenticationPrincipal CustomUserDetails user,@RequestHeader(value="X-Session-Id",required=false)String sessionId){return ApiResponse.success("Danh sách hội thoại",service.list(user==null?null:user.getUserId(),sessionId));}
    @GetMapping("/conversations/{id}")
    public ApiResponse<ChatConversationResponse> get(@AuthenticationPrincipal CustomUserDetails user,@RequestHeader(value="X-Session-Id",required=false)String sessionId,@PathVariable Long id){return ApiResponse.success("Chi tiết hội thoại",service.getConversation(user==null?null:user.getUserId(),sessionId,id));}
    @PatchMapping("/conversations/{id}/close")
    public ApiResponse<ChatConversationResponse> close(@AuthenticationPrincipal CustomUserDetails user,@RequestHeader(value="X-Session-Id",required=false)String sessionId,@PathVariable Long id){return ApiResponse.success("Đã đóng hội thoại",service.close(user==null?null:user.getUserId(),sessionId,id));}
}
