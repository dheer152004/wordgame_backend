package com.example.WordGame.modules.wordrelation;
// package com.example.WordGame.Controller;

// import com.example.WordGame.DTO.WordRelationDTO.WordRelationRequestDTO;
// import com.example.WordGame.DTO.WordRelationDTO.WordRelationResponseDTO;
// import com.example.WordGame.Service.WordRelationService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping(value = "/api/word-relations", produces = "application/json")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// public class WordRelationController {

//     private final WordRelationService wordRelationService;

//     @GetMapping
//     public ResponseEntity<List<WordRelationResponseDTO>> getAllRelations() {
//         return ResponseEntity.ok(wordRelationService.getAllRelations());
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<WordRelationResponseDTO> getRelationById(@PathVariable Long id) {
//         return ResponseEntity.ok(wordRelationService.getRelationById(id));
//     }

//     @PostMapping
//     public ResponseEntity<WordRelationResponseDTO> createRelation(@RequestBody WordRelationRequestDTO request) {
//         return ResponseEntity.status(HttpStatus.CREATED).body(wordRelationService.createRelation(request));
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<WordRelationResponseDTO> updateRelation(
//             @PathVariable Long id,
//             @RequestBody WordRelationRequestDTO request) {
//         return ResponseEntity.ok(wordRelationService.updateRelation(id, request));
//     }

//     @PatchMapping("/{id}")
//     public ResponseEntity<WordRelationResponseDTO> patchRelation(
//             @PathVariable Long id,
//             @RequestBody WordRelationRequestDTO request) {
//         return ResponseEntity.ok(wordRelationService.patchRelation(id, request));
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Map<String, String>> deleteRelation(@PathVariable Long id) {
//         wordRelationService.deleteRelation(id);
//         Map<String, String> response = new HashMap<>();
//         response.put("message", "Word relation deleted successfully");
//         return ResponseEntity.ok(response);
//     }
// }