package ru.yandex.blog_app.model.view;

public interface PostView {

    public static interface Summary {}
    public static interface Details extends Summary {}

    public static interface Create {}
    public static interface Modify {}
}
