package com.example.demo.subject.service.interfaces;

import com.example.demo.subject.dto.request.SubjectRequest;
import com.example.demo.subject.dto.response.SubjectResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SubjectService {
    SubjectResponse create(SubjectRequest request);

    List<SubjectResponse> getAll();

    SubjectResponse getById(Integer id);

    SubjectResponse update(Integer id, SubjectRequest request);

    void delete(Integer id);

    CompletableFuture<List<SubjectResponse>> getAllAsync();
}
