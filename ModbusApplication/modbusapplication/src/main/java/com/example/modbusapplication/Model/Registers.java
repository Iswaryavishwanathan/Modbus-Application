package com.example.modbusapplication.Model;

public class Registers {
    private int reg0;
    private int reg1;
    private int reg2;
    private int reg3;
    private int reg4;
    private int reg5;
    private int reg6;
    private int reg7;
    private int reg8;
    private int reg9;

    public Registers(int... values) {
        if (values.length >= 10) {
            this.reg0 = values[0];
            this.reg1 = values[1];
            this.reg2 = values[2];
            this.reg3 = values[3];
            this.reg4 = values[4];
            this.reg5 = values[5];
            this.reg6 = values[6];
            this.reg7 = values[7];
            this.reg8 = values[8];
            this.reg9 = values[9];
        }
    }

    // Getters
    public int getReg0() { return reg0; }
    public int getReg1() { return reg1; }
    public int getReg2() { return reg2; }
    public int getReg3() { return reg3; }
    public int getReg4() { return reg4; }
    public int getReg5() { return reg5; }
    public int getReg6() { return reg6; }
    public int getReg7() { return reg7; }
    public int getReg8() { return reg8; }
    public int getReg9() { return reg9; }
}
