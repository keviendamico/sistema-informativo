package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.DocumentTemplate;
import com.rextart.sys.sistemainformativo.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTemplateService {

    private final DocumentTemplateRepository repository;

    @Transactional(readOnly = true)
    public List<DocumentTemplate> findAll() {
        return repository.findAllByOrderByDisplayNameAsc();
    }

    @Transactional(readOnly = true)
    public DocumentTemplate getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));
    }

    @Transactional
    public void upload(MultipartFile file, String displayName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        DocumentTemplate t = new DocumentTemplate();
        t.setDisplayName(displayName.trim());
        t.setFilename(StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"));
        t.setContentType(file.getContentType());
        t.setData(file.getBytes());
        repository.save(t);
        log.info("Template '{}' uploaded ({} bytes)", displayName, file.getSize());
    }

    @Transactional
    public void rename(Long id, String newDisplayName) {
        DocumentTemplate t = getById(id);
        t.setDisplayName(newDisplayName.trim());
        repository.save(t);
        log.info("Template {} renamed to '{}'", id, newDisplayName);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Template not found: " + id);
        }
        repository.deleteById(id);
        log.info("Template {} deleted", id);
    }
}