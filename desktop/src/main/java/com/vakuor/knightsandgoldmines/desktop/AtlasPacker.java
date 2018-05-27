package com.vakuor.knightsandgoldmines.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class AtlasPacker {
    public static void main (String[] args) {
        //TexturePacker.process("D:\Projects\KaG\input/Block", "visual/output/Block", "Block");
        //TexturePacker.process("D:\Projects\KaG\input/Head", "visual/output/Head", "Heads");
        //TexturePacker.process("D:\Projects\KaG\input/Archer", "visual/output/Archer", "Archers2");
        TexturePacker.process("D:\\Projects\\KaG\\input/Enemy", "visual/output/Enemy", "Enemy");
        //TexturePacker.process("D:\Projects\KaG\input/flatDark", "visual/output/flatDark", "Controls");
    }
}
