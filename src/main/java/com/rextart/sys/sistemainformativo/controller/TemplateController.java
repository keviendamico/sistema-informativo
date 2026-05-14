package com.rextart.sys.sistemainformativo.controller;

import com.rextart.sys.sistemainformativo.model.DocumentTemplate;
import com.rextart.sys.sistemainformativo.service.DocumentTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final DocumentTemplateService documentTemplateService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", documentTemplateService.findAll());
        model.addAttribute("pageTitle", "Template");
        model.addAttribute("activePage", "templates");
        return "template/list";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails principal) {
        DocumentTemplate t = documentTemplateService.getById(id);
        log.info("Template '{}' downloaded by '{}'", t.getDisplayName(), principal.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + t.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(t.getContentType()))
                .body(t.getData());
    }
}