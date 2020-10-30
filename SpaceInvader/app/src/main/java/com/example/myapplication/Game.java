package com.example.myapplication;

public class Game {
    private int score;
    private int life;
    private int lvl;

    public int getLife() {
        return life;
    }

    public int getLvl() {
        return lvl;
    }

    public int getScore() {
        return score;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
