package com.fandom.services;

import com.dropbox.core.DbxException;
import com.fandom.model.Comment;
import com.fandom.model.Media;
import com.fandom.model.PostState;
import com.fandom.model.PostType;
import com.fandom.repository.CommentRepository;
import com.fandom.util.DropboxUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServices {
    private CommentRepository commentRepository;

    @Autowired
    public CommentServices(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }

    public int countComment(String targetId){
        return commentRepository.countCommentByTargetId(targetId);
    }

    public void addTextComment(String author, String targetId, String content){
        Comment comment = new Comment(author, targetId, content);
        commentRepository.save(comment);
    }

    public void addImageComment(String author, String targetId, String content, MultipartFile[] files){
        List<Media> imgList = new ArrayList<>();
        Arrays.asList(files).forEach(file -> {
            try {
                imgList.add(DropboxUtils.uploadFile(file.getInputStream(), System.currentTimeMillis() + ""));
            } catch (DbxException | IOException e) {
                e.printStackTrace();
            }
        });
        Comment comment = new Comment(author, targetId, content, imgList.toArray(new Media[0]));
        commentRepository.save(comment);
    }

    public void updateTextComment(String author, String targetId, String content){
        Comment comment = commentRepository.findCommentByAuthorAndTargetId(author, targetId);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    public void updateImgComment(String author, String targetId, String content, Media[] img){
        Comment comment = commentRepository.findCommentByAuthorAndTargetId(author, targetId);
        comment.setContent(content);
        comment.setImage(img);
        commentRepository.save(comment);
    }

    public List<Comment> getComments(String targetId, int page){
        Pageable pageable = PageRequest.of(page, 10);
        Page<Comment> commentPage = commentRepository.getCommentsByTargetIdAndState(targetId, PostState.APPROVED, pageable);
        return commentPage.getContent();
    }

    public void deleteComment(String id) throws Exception {
        Optional<Comment> comment = commentRepository.findById(id);
        if(comment.isPresent()){
            Comment c = comment.get();
            c.setState(PostState.DELETED);
            commentRepository.save(c);
        }else throw new Exception();
    }
}
