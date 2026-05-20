package com.example.biblioteca.dto;

import java.util.List;

public record GoogleBooksResponseDTO(List<Item> items) {
    public record Item(VolumeInfo volumeInfo) {}
    public record VolumeInfo(String title, List<String> authors, String publisher, List<String> categories) {}
}