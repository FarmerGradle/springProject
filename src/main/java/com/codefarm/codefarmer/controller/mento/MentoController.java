package com.codefarm.codefarmer.controller.mento;


import com.codefarm.codefarmer.domain.chat.ChatDTO;
import com.codefarm.codefarmer.domain.mentor.MentorBoardDTO;
import com.codefarm.codefarmer.domain.mentor.MentorDTO;
import com.codefarm.codefarmer.domain.mentor.MentorMenteeDTO;
import com.codefarm.codefarmer.entity.chat.Chat;
import com.codefarm.codefarmer.entity.member.Member;
import com.codefarm.codefarmer.entity.mentor.MentorBoard;
import com.codefarm.codefarmer.repository.chat.ChatRepository;
import com.codefarm.codefarmer.repository.mentor.MentorRepository;
import com.codefarm.codefarmer.service.chat.ChatRoomService;
import com.codefarm.codefarmer.service.mentor.MentorMenteeApplyService;
import com.codefarm.codefarmer.service.mentor.MentorService;
import com.codefarm.codefarmer.type.MemberType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.xml.ws.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/mento/*")
public class MentoController {
    private final ChatRoomService cs;
    private final MentorService ms;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate template;
    private final MentorService mentorService;
    private final MentorRepository mentorRepository;
    private final MentorMenteeApplyService mentorMenteeApplyService;

    public MentoController(ChatRoomService cs, MentorService ms, ChatRepository chatRepository, SimpMessagingTemplate template, MentorService mentorService, MentorRepository mentorRepository, MentorMenteeApplyService mentorMenteeApplyService) {
        this.cs = cs;
        this.ms = ms;
        this.chatRepository = chatRepository;
        this.template = template;
        this.mentorService = mentorService;
        this.mentorRepository = mentorRepository;
        this.mentorMenteeApplyService = mentorMenteeApplyService;
    }


    @GetMapping("/intro")
    public void mentoIntro(Model model) {
        List<MentorBoardDTO> mentorBoardDTOs = mentorService.findMainList();
        List<Long> mentorBoardIds = new ArrayList<>();
        List<Double> mentorBoardAvg = new ArrayList<>();
        List<Long> mentorBoardTotalCount = new ArrayList<>();

        for (MentorBoardDTO mentorBoardDTO : mentorBoardDTOs){
            mentorBoardDTO.setFiles(mentorService.showFiles(mentorBoardDTO.getMentorBoardId()));
            mentorBoardIds.add(mentorBoardDTO.getMentorBoardId());
        }

        for(Long mentorBoardId : mentorBoardIds){
            mentorBoardAvg.add(mentorService.findReviewAvg(mentorBoardId).get(0));
            mentorBoardTotalCount.add(mentorService.findReviewCount(mentorBoardId).get(0));
        }

        model.addAttribute("mentorBoardAvg", mentorBoardAvg);
        model.addAttribute("mentorBoardTotalCount", mentorBoardTotalCount);

        model.addAttribute("lists", mentorBoardDTOs);

    }

    @GetMapping("/list")
    public void list(Model model, HttpSession session){
        Long memberId = (Long)session.getAttribute("memberId");
        Object memberType = (Object)session.getAttribute("memberType");
        model.addAttribute("sessionMemberType", memberType);
        model.addAttribute("sessionMemberId", memberId);


    }

    @GetMapping("/write")
    public void write(Model model, HttpSession session){
        Long sessionId = (Long)session.getAttribute("memberId");
        model.addAttribute("mentorBoard", new MentorBoardDTO());
    }

    @PostMapping("/write")
    public RedirectView writeFin(MentorBoardDTO mentorBoardDTO, RedirectAttributes redirectAttributes, HttpSession session){
        Long sessionId = (Long)session.getAttribute("memberId");
        mentorBoardDTO.setMemberId(sessionId);
        mentorBoardDTO.setMentorId(mentorService.findByMemberId(sessionId));
        mentorService.mentorBoardAdd(mentorBoardDTO);
        redirectAttributes.addFlashAttribute("mentorBoardId", mentorBoardDTO.getMentorBoardId());

        return new RedirectView("/mento/list");
    }
    @GetMapping("/detail")
    public void detail(Model model,Long mentorBoardId ,HttpSession session){
        /*????????? ???????????? ????????? ?????? ????????? program JSON ???????????? ??????(????????? boardNumber?????? ????????? ?????? ????????? ?????? ??????)*/
        model.addAttribute("mentorId", ms.showDetailMentorBoard(mentorBoardId).getMentorId());
//        MentorBoardDTO info = mentorService.getInfo();
//      ?????? ??????????????? ????????????
        MentorBoardDTO list = mentorService.showDetailMentorBoard(mentorBoardId);
        Long writeMemberId = mentorService.findmentorByBoardId(mentorBoardId);
        Long mentorId = mentorService.findmentoInfo(writeMemberId);
        MentorDTO mentorDTO = mentorService.findBymentoId(mentorId);
        /*????????? ?????? ????????? ?????????*/
        Long sessionId = (Long) session.getAttribute("memberId");
//        Long sessionId = 1L;
        model.addAttribute("mentorDTO" , mentorDTO);
        model.addAttribute("sessionId", sessionId);
//        ?????? ??????
        model.addAttribute("list", list);
    }


//    ?????? ?????? ????????? ??????
    @GetMapping("/update")
    public String update(Model model, @RequestParam Long mentorBoardId){
        MentorBoardDTO updateRegister = mentorService.showUpdate(mentorBoardId);
        model.addAttribute("updateRegister", updateRegister);
        return "/mento/update";
    }

//    ?????? ???????????? ???????????? ?????? ???
    @PostMapping("/update")
    public RedirectView updateFin(@RequestParam Long mentorBoardId, Long mentorId ,MentorBoardDTO mentorBoardDTO, HttpSession session, String mentorCareer){
        Long sessionId = (Long)session.getAttribute("memberId");
//        ????????? memberId??????
        mentorBoardDTO.setMentorId(mentorService.showUpdate(mentorBoardId).getMentorId());
        mentorBoardDTO.setMemberId(sessionId);
        mentorBoardDTO.setMentorBoardId(mentorBoardId);
        log.info("??????????????? : " + sessionId);
        log.info("??????????????? : " + mentorBoardId);
        log.info("??????????????? : " + mentorService.showUpdate(mentorBoardId).getMentorId());


//        MentorBoard mentorBoard = mentorBoardDTO.toEntity();
//        mentorBoard.update(mentorBoardDTO);
        mentorService.update(mentorBoardDTO);
        return new RedirectView("list");
    }

//    ?????? ?????? ??????
    @GetMapping("/delete")
    public RedirectView delete(@RequestParam Long mentorBoardId){

        mentorService.removeMentorBoard(mentorBoardId);
        return new RedirectView("/mento/list");
    }

//    ?????? ??????
    @GetMapping("/apply")
    public void doApply(Model model, HttpSession session){
        model.addAttribute("mentorMenteeDTO", new MentorMenteeDTO());
    }


//    ???????????? ?????? ?????? ???(Type??? USER??? MENTEE ?????? ???????????? ?????? ??????)
    @PostMapping("/apply")
    public RedirectView apply(MentorMenteeDTO mentorMenteeDTO,@RequestParam Long mentorId,@RequestParam String mentorComment ,HttpSession session){
        Long sessionId = (Long)session.getAttribute("memberId");
        log.info("??????????????? : " + mentorId);
        log.info("??????????????? : " + mentorComment);
        log.info("??????????????? : " + sessionId);

//        MemberType sessionType = (MemberType)session.getAttribute("memberType");
//
//        if(sessionType == MemberType.USER || sessionType == MemberType.MENTEE){

            mentorMenteeDTO.setMenteeId(sessionId);

            mentorMenteeDTO.setMentorId(mentorId);

            mentorMenteeDTO.setMenteeComment(mentorComment);

            mentorMenteeApplyService.saveMenteeApply(mentorMenteeDTO);

//        }
        return new RedirectView("/mento/list");
    }



    /*????????? ?????? ???*/
    @GetMapping("/chatting")
    @RequestMapping(value = "/mento/chatting", method = RequestMethod.GET)
    public void chatting(@RequestParam(value="mentorId", required=false, defaultValue="") Long mentorId, Model model, HttpSession session) {
        /*????????? ?????? ????????? ?????????*/
        Long sessionId = (Long) session.getAttribute("memberId");
        model.addAttribute("sessionId", sessionId);

        /*????????? ?????? ???????????? ?????? ????????? ??????*/
        if(mentorId != null) { // ?????? ????????? ?????????????????? ?????????????????? ???????????? ?????? ????????? ID??? ??????
            cs.createChatRoom(mentorId, sessionId); // ???????????? ????????? ?????? ?????????????????? ????????? ??????
        }
        /*????????? ?????? ????????? ?????? ?????? ????????? ?????? ??????*/
        model.addAttribute("rooms", cs.chatRoomSelectAll(sessionId));
        /*????????? ????????? ?????? ?????? ?????? ????????? ?????? ????????????*/
        model.addAttribute("alarmCnt", cs.chatAlarm(sessionId)); // ????????? ????????? ?????? ?????? ???????????? ?????? ??????(?????? ??????????????? ?????? ??????)
    }
}
















