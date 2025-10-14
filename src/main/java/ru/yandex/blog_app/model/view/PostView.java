package ru.yandex.blog_app.model.view;

public interface PostView {

    public static interface Summary {}
    public static interface Detail extends Summary {}

    public static interface Update {}
    public static interface Create {}
}
