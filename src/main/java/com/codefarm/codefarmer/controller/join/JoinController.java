package com.codefarm.codefarmer.controller.join;

import com.codefarm.codefarmer.domain.member.MemberDTO;
import com.codefarm.codefarmer.domain.oauth.GoogleOAuthRequest;
import com.codefarm.codefarmer.domain.oauth.GoogleOAuthResponse;
import com.codefarm.codefarmer.repository.member.MemberRepository;
import com.codefarm.codefarmer.service.join.JoinGoogleService;
import com.codefarm.codefarmer.service.join.JoinKakaoService;
import com.codefarm.codefarmer.service.join.JoinNaverService;
import com.codefarm.codefarmer.service.join.JoinService;
import com.codefarm.codefarmer.service.member.MemberService;
import com.codefarm.codefarmer.type.MemberType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/register/*")
@RequiredArgsConstructor
public class JoinController {

    private final JoinKakaoService joinKakaoService;
    private final JoinNaverService joinNaverService;
    private final JoinGoogleService joinGoogleService;
    private final JoinService joinService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("")
    public String joinPage(){
        return "/join/join";
    }

    @ResponseBody
    @GetMapping("/kakao")
    public RedirectView kakaoLogin(String code, RedirectAttributes redirectAttributes,HttpSession session){
        log.info("?????? : "+code);
        String token = joinKakaoService.getKakaoAccessToken(code);
        session.setAttribute("token", token);
        try {
            String memberEmail = joinKakaoService.getKakaoEmailByToken(token);
            Long id = joinKakaoService.getKakaoIdByToken(token);
            String memberOauthId = id+"k";
            session.setAttribute("oauthId", memberOauthId);
            log.info("?????????: " + memberOauthId);
            log.info("?????????: " + memberEmail);
            redirectAttributes.addFlashAttribute("memberOauthId", memberOauthId);
            redirectAttributes.addFlashAttribute("memberEmail", memberEmail);
            if(joinService.checkOauth(memberOauthId) == 1){
                return new RedirectView("/login/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RedirectView("/register/form");
    }

    @GetMapping("/form")
    public String joinFormPage(MemberDTO memberDTO){
        return "/join/joinForm";
    }

    @GetMapping("/agreement")
    public String agreementPage(){
        return "/join/agreement";
    }

    @ResponseBody
    @PostMapping("/form")
    public Integer checkUserNick(@RequestParam("memberNickname") String nickname){
        if(joinService.checkUserNick(nickname) == 1){
            return 1;
        }else {
            return 0;
        }
    }


    @PostMapping("/joinOk")
    public RedirectView joinOk(MemberDTO memberDTO, HttpSession session){
        memberService.join(memberDTO);
        Long id = joinService.selectId(memberDTO.getMemberOauthId());
        String type = joinService.selectType(memberDTO.getMemberOauthId());
        log.info("??????11:"+memberDTO);
        log.info("??????95:"+memberDTO.getMemberType());



        session.setAttribute("memberId", id);
        session.setAttribute("memberType", type);
        Long value = (Long)session.getAttribute("memberId");
        log.info("?????? value"+ value);
        log.info("?????? stringv"+ type);
        return new RedirectView("/main");
    }


    @ResponseBody
    @GetMapping("/naver")
    public RedirectView naverLogin(String code, RedirectAttributes redirectAttributes,HttpSession session){
        log.info("?????? : "+code);
        String token = joinNaverService.getNaverAccessToken(code);
        session.setAttribute("token", token);
        try {
            String memberEmail = joinNaverService.getNaverEmailByToken(token);
            String id = joinNaverService.getNaverIdByToken(token);
            String memberOauthId = id+"n";
            session.setAttribute("oauthId", memberOauthId);
            log.info("?????????: " + memberOauthId);
            log.info("?????????: " + memberEmail);
            redirectAttributes.addFlashAttribute("memberOauthId", memberOauthId);
            redirectAttributes.addFlashAttribute("memberEmail", memberEmail);
            if(joinService.checkOauth(memberOauthId) == 1){
                return new RedirectView("/login/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RedirectView("/register/form");
    }

    @RequestMapping(value = "/google", method = RequestMethod.GET)
    public RedirectView googleAuth(Model model, @RequestParam(value = "code") String authCode, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) throws Exception {
        System.out.println("?????????");
        //HTTP Request??? ?????? RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        //Google OAuth Access Token ????????? ?????? ???????????? ??????
        GoogleOAuthRequest googleOAuthRequestParam =  new GoogleOAuthRequest();
        googleOAuthRequestParam.setClientId("467034188557-a2dk8gl9s7rtj2nvh0f0t5ls35gnfsi3.apps.googleusercontent.com");
        googleOAuthRequestParam.setClientSecret("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");
        googleOAuthRequestParam.setCode(authCode);
        googleOAuthRequestParam.setRedirectUri("http://localhost:5555/register/google");
        googleOAuthRequestParam.setGrantType("authorization_code");

        //JSON ????????? ?????? ????????? ??????
        //????????? ??????????????? ???????????? ???????????? ??????????????? Object mapper??? ?????? ???????????????.
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        //AccessToken ?????? ??????
        ResponseEntity<String> resultEntity = restTemplate.postForEntity("https://oauth2.googleapis.com/token", googleOAuthRequestParam, String.class);

        //Token Request
        GoogleOAuthResponse result = mapper.readValue(resultEntity.getBody(), new TypeReference<GoogleOAuthResponse>() {
        });

        //ID Token??? ?????? (???????????? ????????? jwt??? ????????? ????????????)
        String jwtToken = result.getIdToken();
        String requestUrl = UriComponentsBuilder.fromHttpUrl("https://oauth2.googleapis.com/tokeninfo").queryParam("id_token", jwtToken).toUriString();

        String resultJson = restTemplate.getForObject(requestUrl, String.class);

        Map<String,String> userInfo = mapper.readValue(resultJson, new TypeReference<Map<String, String>>(){});
        model.addAllAttributes(userInfo);
        model.addAttribute("token", result.getAccessToken());


        System.out.println("?????? : " + result.getAccessToken());
        session.setAttribute("oauthId", result.getAccessToken() + "g");
        log.info("?????????: " + result.getAccessToken() + "g");
        log.info("?????????: " + userInfo.get("email"));
        redirectAttributes.addFlashAttribute("memberOauthId", result.getAccessToken() + "g");
        redirectAttributes.addFlashAttribute("memberEmail", userInfo.get("email"));


        if(memberRepository.findByEmail(userInfo.get("email")) > 0) {
            return new RedirectView("/login/");
        }

        return new RedirectView("/register/form");
    }



//    @ResponseBody
//    @GetMapping("/google")
//    public RedirectView googleLogin(String code, RedirectAttributes redirectAttributes,HttpSession session){
//        log.info("?????? join ????????? : "+code);
//        String token = joinGoogleService.getGoogleAccessToken(code);
//        log.info("?????? ??????  ????????? : "+token);
//        session.setAttribute("token", token);
//        try {
//            String memberEmail = joinGoogleService.getGoogleEmailByToken(token);
//            String id = joinGoogleService.getGoogleIdByToken(token);
//            String memberOauthId = id+"n";
//            session.setAttribute("oauthId", memberOauthId);
//            log.info("?????????: " + memberOauthId);
//            log.info("?????????: " + memberEmail);
//            redirectAttributes.addFlashAttribute("memberOauthId", memberOauthId);
//            redirectAttributes.addFlashAttribute("memberEmail", memberEmail);
//            if(joinService.checkOauth(memberOauthId) == 1){
//                System.out.println("?????????2?");
//                return new RedirectView("/login/");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("?????????3?");
//        return new RedirectView("/register/form");
//    }
}
