package com.inProject.in.domain.MToNRelation.ClipBoardRelation.service;

import com.inProject.in.domain.Board.Dto.ResponseBoardListDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.ResponseClipBoardRelationDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClipBoardRelationService {
    List<ResponseBoardListDto> getClipedBoards(Pageable pageable, HttpServletRequest request);
    ResponseClipBoardRelationDto insertClip(Long user_id, Long post_id);
    ResponseClipBoardRelationDto deleteClip(Long user_id, Long post_id);
}
