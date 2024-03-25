delete from user_role;
delete from usr;

insert into usr(id, active, password, username) values
    (1, true,'$2a$08$DTnxbs7F3Cg9DresyuuvyeknDmZNPURI7bTpg66JXT8WVqM9WQPgS', 'admin'),
    (2, true,'$2a$08$DTnxbs7F3Cg9DresyuuvyeknDmZNPURI7bTpg66JXT8WVqM9WQPgS', 'Murschid');

insert into user_role(user_id, roles) values
                                          (1, 'USER'), (1, 'ADMIN'), (2, 'USER');