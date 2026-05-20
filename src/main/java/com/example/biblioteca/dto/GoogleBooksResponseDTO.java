package com.example.biblioteca.dto;

public record GoogleBooksResponseDTO(java.util.List<Item> items) {
    public record Item(VolumeInfo volumeInfo) {}
    public record VolumeInfo(String title, java.util.List<String> authors, String publisher, java.util.List<String> categories) {}
}
