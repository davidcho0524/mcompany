package com.teacher.management.service;

import jakarta.annotation.PostConstruct;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.domain}")
    private String domain;

    @Value("${solapi.from-number}")
    private String fromNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
    }

    public String sendSms(String to, String text) {
        return sendMessage(to, text, "SMS", null, null);
    }

    public String sendKakao(String to, String text, String pfId, String templateId) {
        return sendMessage(to, text, "KAKAO", pfId, templateId);
    }

    public String sendMessage(String to, String text, String type, String pfId, String templateId) {
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);

        if (text != null && !text.isEmpty()) {
            message.setText(text);
        }

        if ("KAKAO".equalsIgnoreCase(type)) {
            // Check for Kakao options
            if (pfId == null || templateId == null) {
                throw new IllegalArgumentException("PfId and TemplateId are required for Kakao messages");
            }
            net.nurigo.sdk.message.model.KakaoOption kakaoOption = new net.nurigo.sdk.message.model.KakaoOption();
            kakaoOption.setPfId(pfId);
            kakaoOption.setTemplateId(templateId);
            // kakaoOption.setDisableSms(true); // Optional: Disable SMS fallback
            message.setKakaoOptions(kakaoOption);
        }

        // Just use SDK 'sendOne'
        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println("Solapi Response: " + response); // Debug log
        return response.getMessageId();
    }
}
