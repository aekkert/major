/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.tbot;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 *
 * @author alex
 */
public final class StatCommand extends CustomCommand {

    public StatCommand() {
        super("stat", "Статистика @insta_major\n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Bot b = Bot.getInstance();
        b.statMenu(chat.getId());
    }
    
}
