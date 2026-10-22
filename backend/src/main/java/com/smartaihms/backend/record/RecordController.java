package com.smartaihms.backend.record;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public RecordView create(@RequestBody RecordRequest request) {
        return recordService.create(request);
    }

    @GetMapping("/mine")
    public List<RecordView> mine() {
        return recordService.mine();
    }

    @GetMapping("/patient/{patientId}")
    public List<RecordView> forPatient(@PathVariable String patientId) {
        return recordService.forPatient(patientId);
    }
}
