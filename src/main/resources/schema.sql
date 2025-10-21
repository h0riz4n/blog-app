create schema if not exists blog_app;

create table if not exists blog_app.post (
    id bigserial primary key,
    title varchar(50) not null,
    text text not null,
    likes_count bigint not null,
    file_name text
);

create table if not exists blog_app.tag (
    id bigserial primary key,
    post_id bigint not null,
    text varchar(50) not null,
    constraint tag_post_id_fk foreign key (post_id) references blog_app.post(id) on delete cascade
);

create table if not exists blog_app.comment (
    id bigserial primary key,
    post_id bigint not null,
    text text not null,
    constraint comment_post_id_fk foreign key (post_id) references blog_app.post(id) on delete cascade
);
