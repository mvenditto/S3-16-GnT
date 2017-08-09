package com.unibo.s3.main_system.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.BasicMovableEntity;

public class BaseCharacter extends BasicMovableEntity implements Character {

    private Color color;
    private final int id;

    public BaseCharacter(Vector2 position, int id) {
        super(position);
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

}
