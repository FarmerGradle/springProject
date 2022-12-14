package com.codefarm.codefarmer.repository.member;

import com.codefarm.codefarmer.domain.alba.AlbaDTO;
import com.codefarm.codefarmer.domain.alba.MemberAlbaDTO;
import com.codefarm.codefarmer.domain.board.BoardDTO;
import com.codefarm.codefarmer.domain.inquire.InquireAnswerDTO;
import com.codefarm.codefarmer.domain.inquire.InquireDTO;
import com.codefarm.codefarmer.domain.program.MemberProgramDTO;
import com.codefarm.codefarmer.domain.program.ProgramDTO;
import com.codefarm.codefarmer.entity.alba.Alba;
import com.codefarm.codefarmer.entity.alba.MemberAlba;
import com.codefarm.codefarmer.entity.board.Board;
import com.codefarm.codefarmer.entity.inquire.Inquire;
import com.codefarm.codefarmer.entity.inquire.InquireAnswer;
import com.codefarm.codefarmer.entity.member.Member;
import com.codefarm.codefarmer.entity.program.MemberProgram;
import com.codefarm.codefarmer.entity.program.Program;
import com.codefarm.codefarmer.type.Status;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberCustomRepository {
    //닉네임 중복검사
    public Integer duplicateNick(String nickname);
    //내가 등록한 프로그램 신청자 select
    public List<MemberProgramDTO> selectMyProgramApplyers(Long memberId, Long programId);
    //내가 등록한 프로그램 신청자 select
    public List<MemberAlbaDTO> selectMyAlbaApplyers(Long memberId, Long albaId);
    //oauthid 검사
    public Integer duplicateOauth(String oauthId);
    //내가 등록한 알바목록
    public List<AlbaDTO> selectMyAlba(Long memberId);
    //내가 등록한 프로그램목록
    public List<ProgramDTO> selectMyProgram(Long memberId);
    //내가 등록한 게시글
    public List<BoardDTO> selectMyBoard(Long memberId);
    //내가 문의한 목록
    public List<InquireDTO> selectMyInquire(Long memberId);
    //알바 신청 목록
    public List<MemberAlbaDTO> selectMyAlbaApply(Long memberId);
    //프로그램 신청 목록
    public List<MemberProgramDTO> selectMyProgramApply(Long memberId);


}
