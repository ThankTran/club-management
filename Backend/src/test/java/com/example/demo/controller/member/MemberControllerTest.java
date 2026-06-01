package com.example.demo.controller.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.application.dto.response.department.DepartmentResponse;
import com.example.demo.application.service.department.interfaces.DepartmentService;
import com.example.demo.application.service.member.interfaces.MemberService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class MemberControllerTest {
    @Test
    void deleteMemberReturnsNoContent() {
        MemberService memberService = Mockito.mock(MemberService.class);
        DepartmentService departmentService = Mockito.mock(DepartmentService.class);
        MemberController controller = new MemberController(memberService, departmentService);

        var response = controller.deleteMember(12L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(memberService).deleteMember(12L);
    }

    @Test
    void getAllDepartmentsReturnsDataFromService() {
        MemberService memberService = Mockito.mock(MemberService.class);
        DepartmentService departmentService = Mockito.mock(DepartmentService.class);
        MemberController controller = new MemberController(memberService, departmentService);
        when(departmentService.getAll()).thenReturn(List.of(DepartmentResponse.builder().departmentId(1L).departmentName("Test").build()));

        var response = controller.getAllDepartments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
