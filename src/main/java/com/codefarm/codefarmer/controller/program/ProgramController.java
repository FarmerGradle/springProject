package com.codefarm.codefarmer.controller.program;

import com.codefarm.codefarmer.domain.member.MemberDTO;
import com.codefarm.codefarmer.domain.program.MemberProgramDTO;
import com.codefarm.codefarmer.domain.program.ProgramDTO;
import com.codefarm.codefarmer.domain.program.ProgramFileDTO;
import com.codefarm.codefarmer.entity.member.Member;
import com.codefarm.codefarmer.entity.mentor.Mentor;
import com.codefarm.codefarmer.entity.program.MemberProgram;
import com.codefarm.codefarmer.entity.program.Program;
import com.codefarm.codefarmer.entity.program.ProgramFile;
import com.codefarm.codefarmer.repository.member.MemberRepository;
import com.codefarm.codefarmer.repository.mentor.MentorRepository;
import com.codefarm.codefarmer.repository.program.ProgramFileRepository;
import com.codefarm.codefarmer.service.member.MemberService;
import com.codefarm.codefarmer.service.program.*;
import com.codefarm.codefarmer.type.ProgramLevel;
import com.codefarm.codefarmer.type.ProgramStatus;
import com.codefarm.codefarmer.type.ProgramType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.zip.DataFormatException;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/program/*")
public class ProgramController {

    private final ProgramListService programListService;
    private final ProgramDetailService programDetailService;
    private final ProgramRegisterService programRegisterService;
    private final ProgramFileRepository programFileRepository;
    private final ProgramUpdateService programUpdateService;
    private final ProgramDeleteService programDeleteService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final MentorRepository mentorRepository;
    private final ProgramPayService programPayService;

    @GetMapping("/list")
    public void list(Model model){
        List<ProgramDTO> lists = programListService.showAll();
        model.addAttribute("lists",lists);
    }

    @GetMapping("/detail")
    public void detail(Model model,@RequestParam Long programId , HttpSession session){
        Long memberId = (Long)session.getAttribute("memberId");
        String check = "user";
        log.info("??????????????? ?????????");
        log.info("programId:" + programId);
        Long mentorId = programDetailService.findMemberIdByProgramId(programId);


        ProgramDTO list = programDetailService.showByProgramId(programId);
        list.setFiles(programDetailService.showFiles(programId));

        if(memberId != null) {
            int checkId = programDetailService.programApplyCheck(memberId, programId);
            Long registerCheckId = programDetailService.programRegisterCheck(memberId, programId);
            Long mentorMenteeId = programDetailService.findMentorMenteeTrue(memberId, mentorId);
            if(list.getProgramType().equals(ProgramType.ONLY_MENTEE)){
//            ???????????? ??????????????? ??????
                if(mentorMenteeId == null){
//            ???????????? ????????? ?????? ???
                    check = "isNotMentoMentee";
                    model.addAttribute("check" , check);
                }else if(registerCheckId != null){
//            ?????? ????????? ??????????????? ???
                    check = "register";
                    model.addAttribute("check" , check);
                }else if(checkId == 0){
//            ???????????? ?????? ????????? ???
                    model.addAttribute("check" , check);
                }else{
//            ???????????? ?????? ?????? ???
                    check = "apply";
                    model.addAttribute("check" , check);
                }

            }else{
//            ????????? ????????? ??? ?????? ??????????????? ??????
                if(registerCheckId != null){
//            ?????? ????????? ??????????????? ???
                    check = "register";
                    model.addAttribute("check" , check);
                }else if(checkId == 0){
//            ???????????? ?????? ????????? ???
                    model.addAttribute("check" , check);
                }else{
//            ???????????? ?????? ?????? ???
                    check = "apply";
                    model.addAttribute("check" , check);
                }

            }
        }
        else{

            model.addAttribute("check" , "noLogin");
        }

        //        log.info("null?: " + programDetailService.programApplyCheck(memberId,programId));

//      check
//      user: ?????? ????????? ???
//      apply: ?????? ????????? ???????????? ??? ???
//      register: ?????? ????????? ???????????? ??? ???
//      isNotMentoMentee: ?????? ????????? ?????????





        log.info("????????? ??????: " + list.toString());
//        List<ProgramDTO> lists = programListService.();
        model.addAttribute("list",list);
    }

//    ???????????? ?????? ????????? ??????
    @GetMapping("/apply")
    public void apply(HttpSession session ,@RequestParam Long programId , Model model){
        Long memberId = (Long)session.getAttribute("memberId");
        log.info("memberId:" + memberId);
        Member member = memberService.find(memberId);
        String memberName = member.getMemberName();
        String memberEmail = member.getMemberEmail();
        String memberLocation = member.getMemberLocation();
        String memberPhone = member.getMemberPhone();
        String memberBirth = member.getMemberBirth();
        ProgramDTO programDTO = programDetailService.showByProgramId(programId);
        log.info("????????????DTO:" + programDTO.toString());
        String programTitle = programDTO.getProgramTitle();
        String programCrop = programDTO.getProgramCrop();
        String farmerName = memberService.find(programDTO.getMemberId()).getMemberName();
        String programName = programDTO.getProgramTitle();

        model.addAttribute("memberName" , memberName);
        model.addAttribute("memberEmail" , memberEmail);
        model.addAttribute("memberLocation" , memberLocation);
        model.addAttribute("memberPhone" , memberPhone);
        model.addAttribute("memberBirth" , memberBirth);
        model.addAttribute("farmerName" , farmerName);
        model.addAttribute("programName" , programName);
        model.addAttribute("programTitle" , programTitle);
        model.addAttribute("programCrop" , programCrop);
        model.addAttribute("programId" , programId);
    }

//    ?????? ?????? ???
    @GetMapping("/applyfin")
    public void applyfin(){

    }

    @GetMapping("/pay")
    public String freepay(HttpSession session,@RequestParam Long programId, MemberProgramDTO memberProgramDTO,String programApplyBirthStr){
        Long memberId = (Long)session.getAttribute("memberId");
        Long getProgramId = Long.valueOf(programId);
        log.info("?????? ??? ????????????" + memberProgramDTO.toString());
        memberProgramDTO.setProgramStatus(ProgramStatus.PAY_SUCCEED);
        log.info(programApplyBirthStr);
        log.info("?????????????????????:"+programId);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime programApplyBirths = LocalDateTime.parse(programApplyBirthStr);
        memberProgramDTO.setProgramApplyBirth(programApplyBirths);
        programPayService.applyProgram(memberProgramDTO,getProgramId,memberId);
        return "/program/applyfin";
    }

//    ???????????? ??? ????????????????????? ???????????? ????????????
    @PostMapping("/pay")
    public void pay(HttpSession session, @RequestParam Long programId,Model model ,MemberProgramDTO memberProgramDTO,String programApplyBirthString){
        Long memberId = (Long)session.getAttribute("memberId");
        log.info("memberId:" + memberId);
        Member member = memberService.find(memberId);
        String memberName = member.getMemberName();
        ProgramDTO programDTO = programDetailService.showByProgramId(programId);
        log.info("????????????DTO:" + programDTO.toString());
        String farmerName = memberService.find(programDTO.getMemberId()).getMemberName();
        int programPrice = programDTO.getProgramPrice();
        int programApplyCount = memberProgramDTO.getProgramApplyCount();
        int programApplyTotalCount = programDTO.getProgramApplyTotalCount();
        int programTotalPrice = programPrice * programApplyCount;
        String programName = programDTO.getProgramTitle();
        String memberEmail = memberProgramDTO.getProgramApplyEmail();
        String memberPhone = memberProgramDTO.getProgramApplyPhoneNum();
        String memberLocation = memberService.find(programDTO.getMemberId()).getMemberLocation();

        model.addAttribute("memberName" , memberName);
        model.addAttribute("farmerName" , farmerName);
        model.addAttribute("programApplyCount" , programApplyCount);
        model.addAttribute("programApplyTotalCount" ,programApplyTotalCount);
        model.addAttribute("programPrice" , programPrice);
        model.addAttribute("programTotalPrice" , programTotalPrice);
        model.addAttribute("programName", programName);
        model.addAttribute("memberEmail", memberEmail);
        model.addAttribute("memberPhone", memberPhone);
        model.addAttribute("memberLocation", memberLocation);
        model.addAttribute("programApplyBirthString",programApplyBirthString);
        model.addAttribute("programId", programId);

//        model.addAttribute("programTotalPrice" , programDTO.getProgramPrice() * )


        log.info("????????? ?????? ?????????:" + memberProgramDTO.toString());
        log.info("programApplyBirthString" + programApplyBirthString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime programApplyBirth = LocalDate.parse(programApplyBirthString, formatter).atStartOfDay();
        memberProgramDTO.setProgramApplyBirth(programApplyBirth);
        model.addAttribute("programApplyBirth",programApplyBirth);

        log.info("??????????????????DTO:" + memberProgramDTO.toString());
    }

    @GetMapping("/register")
    public void register(Model model){
        model.addAttribute("programRegister" , new ProgramDTO());
    }

    @PostMapping("/register")
    public RedirectView registerFin(ProgramDTO programDTO,  HttpSession session ,String programWorkDateString, String programWorkStartTimeString, String programWorkEndTimeString, String programApplyStartDateString, String programApplyEndDateString , String programTypeString, String programLevelString) throws DateTimeParseException {
        log.info("?????????????????? ???????????? ?????????");
        Long sessionId = (Long)session.getAttribute("memberId");

//        log.info("fileDTO:" + programFileDTO.toString());

//       ????????? memberId ??????
        programDTO.setMemberId(sessionId);

//        ??? ?????? ??? ????????????,???????????? ?????? DTO??? ??? ??????
        if(programTypeString.equals("????????????")){
            programDTO.setProgramType(ProgramType.ALL_USER);
        }else{
            programDTO.setProgramType(ProgramType.ONLY_MENTEE);
        }

//        ??? ?????? ??? ??????,??????,?????? ?????? DTO??? ??? ??????
        if(programLevelString.equals("??????")){
            programDTO.setProgramLevel(ProgramLevel.BASIC);
        }else if(programLevelString.equals("??????")){
            programDTO.setProgramLevel(ProgramLevel.SKILL_UP);
        }else{
            programDTO.setProgramLevel(ProgramLevel.MASTER);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DateTimeFormatter formatter1 = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .parseDefaulting(ChronoField.EPOCH_DAY, 0)
                .toFormatter();

        LocalDateTime programWorkDateTest = LocalDate.parse(programWorkDateString, formatter).atStartOfDay();
        LocalDateTime programWorkStartTimeTest = LocalDateTime.parse(programWorkStartTimeString, formatter1);
        LocalDateTime programWorkEndTimeTest = LocalDateTime.parse(programWorkEndTimeString, formatter1);
        LocalDateTime programApplyStartDateTest = LocalDate.parse(programApplyStartDateString, formatter).atStartOfDay();
        LocalDateTime programApplyEndDateTest = LocalDate.parse(programApplyEndDateString, formatter).atStartOfDay();

        programDTO.setProgramWorkDate(programWorkDateTest);
        programDTO.setProgramWorkStartTime(programWorkStartTimeTest);
        programDTO.setProgramWorkEndTime(programWorkEndTimeTest);
        programDTO.setProgramApplyStartDate(programApplyStartDateTest);
        programDTO.setProgramApplyEndDate(programApplyEndDateTest);

        programRegisterService.saveAll(programDTO);
        return new RedirectView("list");
    }

//    ???????????? ????????? ??????
    @GetMapping("/update")
    public void update(Model model , @RequestParam Long programId){
        ProgramDTO updateRegister = programUpdateService.showUpdate(programId);
        model.addAttribute("updateRegister" , updateRegister);
    }

//    ???????????? ???????????? ???????????? ?????? ?????? ???
    @PostMapping("/update")
    public RedirectView updateFin(@RequestParam Long programId, ProgramDTO programDTO ,HttpSession session , String programWorkDateString, String programWorkStartTimeString, String programWorkEndTimeString, String programApplyStartDateString, String programApplyEndDateString , String programTypeString, String programLevelString){
        log.info("?????????????????? ???????????? ?????????");
        log.info("programTypeString: " + programTypeString);
        log.info("programLevelString: " + programLevelString);
        Long sessionId = (Long)session.getAttribute("memberId");

//       ????????? memberId ??????
        programDTO.setMemberId(sessionId);

//        ??? ?????? ??? ????????????,???????????? ?????? DTO??? ??? ??????
        if(programTypeString.equals("????????????")){
            programDTO.setProgramType(ProgramType.ALL_USER);
        }else{
            programDTO.setProgramType(ProgramType.ONLY_MENTEE);
        }

//        ??? ?????? ??? ??????,??????,?????? ?????? DTO??? ??? ??????
        if(programLevelString.equals("??????")){
            programDTO.setProgramLevel(ProgramLevel.BASIC);
        }else if(programLevelString.equals("??????")){
            programDTO.setProgramLevel(ProgramLevel.SKILL_UP);
        }else{
            programDTO.setProgramLevel(ProgramLevel.MASTER);
        }

//        log.info("files: " + programDTO.getFiles());
//        programDTO.getFiles().stream().map(t -> programFileDTO.setFileName(t.getFileName()));
//        log.info("file[0].fileName:"+ programDTO.getFiles().get(0).getFileName());
//        log.info("file[0].fileName:"+ programDTO.getFiles().get(0).getFileUploadPath());
//        log.info("file[0].fileName:"+ programDTO.getFiles().get(0).getFileUuid());
//        log.info("file[0].fileName:"+ programDTO.getFiles().get(0).getFileSize());

//      ?????? ??????
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DateTimeFormatter formatter1 = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .parseDefaulting(ChronoField.EPOCH_DAY, 0)
                .toFormatter();

//        ?????? ??????
        LocalDateTime programWorkDateTest = LocalDate.parse(programWorkDateString, formatter).atStartOfDay();

        log.info("1" + programWorkDateTest);
        LocalDateTime programWorkStartTimeTest = LocalDateTime.parse(programWorkStartTimeString, formatter1);
        log.info("2" + programWorkStartTimeTest);
        LocalDateTime programWorkEndTimeTest = LocalDateTime.parse(programWorkEndTimeString, formatter1);
        log.info("3" + programWorkEndTimeTest);
        LocalDateTime programApplyStartDateTest = LocalDate.parse(programApplyStartDateString, formatter).atStartOfDay();
        LocalDateTime programApplyEndDateTest = LocalDate.parse(programApplyEndDateString, formatter).atStartOfDay();

//        ?????? ??????
        programDTO.setProgramWorkDate(programWorkDateTest);
        programDTO.setProgramWorkStartTime(programWorkStartTimeTest);
        programDTO.setProgramWorkEndTime(programWorkEndTimeTest);
        programDTO.setProgramApplyStartDate(programApplyStartDateTest);
        programDTO.setProgramApplyEndDate(programApplyEndDateTest);

        /*Program program = null;
        program.changeFiles(programDTO.getFiles());*/

//        ?????? ??????
        log.info("????????????id ???????????????? : " + programDTO.getProgramId());


        programDTO.setProgramId(programId);
        programUpdateService.update(programDTO);



//        programDTO.getFiles().stream().map(t -> programFileRepository.saveAll(t));


//        programFileRepository.save(programDTO.getFiles())

//        redirectAttributes.addFlashAttribute("boardNumber", boardVO.getBoardNumber());
        return new RedirectView("list");
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String programIdString){
        log.info("delete???????????? ?????????" );
        log.info("programId:" + programIdString);
        Long programId = Long.parseLong(programIdString);
//        File file = new File("C:/upload", uploadPath + "/" + fileName);
//        if(file.exists()){
//            file.delete();
//        }
//
//        if(fileImageCheck) {
//            file = new File("C:/upload", uploadPath + "/s_" + fileName);
//            if (file.exists()) {
//                file.delete();
//            }
//        }
        programDeleteService.programDelete(programId);
        return "redirect:/mypage/program";
    }
}
