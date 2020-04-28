package com.example.blogrestframework;

import java.time.OffsetDateTime;

public class Post implements Comparable<Post>{
    int id;
    String url;
    String title;
    String content;
    OffsetDateTime datePosted;
    int authorId;

    public Post(int id, String url, String title, String content, OffsetDateTime datePosted, int authorId) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.content = content;
        this.datePosted = datePosted;
        this.authorId = authorId;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getDatePosted() {
        return datePosted;
    }

    public int getAuthorId() {
        return authorId;
    }

    @Override
    public int compareTo(Post post) {
        return this.datePosted.compareTo(post.datePosted);
    }
}
