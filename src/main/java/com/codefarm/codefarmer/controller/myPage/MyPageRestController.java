package com.codefarm.codefarmer.controller.myPage;

import com.codefarm.codefarmer.domain.alba.AlbaDTO;
import com.codefarm.codefarmer.domain.board.ReplyDTO;
import com.codefarm.codefarmer.domain.member.MemberDTO;
import com.codefarm.codefarmer.domain.program.ProgramDTO;
import com.codefarm.codefarmer.entity.alba.Alba;
import com.codefarm.codefarmer.entity.board.Board;
import com.codefarm.codefarmer.entity.inquire.Inquire;
import com.codefarm.codefarmer.entity.member.Member;
import com.codefarm.codefarmer.entity.program.Program;
import com.codefarm.codefarmer.service.admin.InquireService;
import com.codefarm.codefarmer.service.member.MemberService;
import com.codefarm.codefarmer.service.program.ProgramListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/mypage/*")
@RequiredArgsConstructor //final이 붙거나 @NotNull 이 붙은 필드의 생성자를 자동 생성해주는 롬복 어노테이션
public class MyPageRestController {

    private final MemberService memberService;
    private final InquireService inquireService;
    private final ProgramListService programListService;

    //닉네임수정
    @PutMapping("/settingnick")
    public String updateNick(@RequestBody MemberDTO memberDTO){
        memberService.updateNick(memberDTO);
        return "update success";
    }

    //프로그램 목록
    @GetMapping("/program")
    public List<ProgramDTO> showAllPgList(HttpSession session) {
        Long memberId = (Long)session.getAttribute("memberId");
        log.info("아이디 : "+ memberId);
        List<ProgramDTO> programs = memberService.registerMyProgram(memberId);
        for (ProgramDTO program : programs){
            program.setFiles(programListService.showFiles(program.getProgramId()));
        }
        log.info("전체 : "  + programs.toString());
        return programs;
    }

    //알바 목록
    @GetMapping("/alba")
    public List<AlbaDTO> showAllAbList(HttpSession session) {
        Long memberId = (Long)session.getAttribute("memberId");
        log.info("아이디 : "+ memberId);
        List<AlbaDTO> albas = memberService.registerMyAlba(memberId);
        return albas;
    }
}
