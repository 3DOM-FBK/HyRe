/**
 * Hybrid Registration (C) 2019 is a command line software designed to
 * analyze, co-register and filter airborne point clouds acquired by LiDAR sensors
 * and photogrammetric algorithm.
 * Copyright (C) 2019  Michele Welponer, mwelponer@gmail.com (Fondazione Bruno Kessler)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <https://www.gnu.org/licenses/> and file GPL3.txt
 *
 * -------------
 * IntelliJ Program arguments:
 * $ContentRoot$/resources/f1.txt $ContentRoot$/resources/f2.txt 1f -w -v
 */
package eu.fbk.threedom.pc;

public enum PointClassification {

    C0(0),
    C1(1),
    C2(2);//,
//    C3(3),
//    C4(4),
//    C5(5),
//    C6(6),
//    C7(7),
//    C8(8),
//    C9(9);

    public int type;

    PointClassification(int type){
        this.type = type;
    }

    public static PointClassification parse(int type){
        switch (type) {
            case 0: return C0; //ROOF
            case 1: return C1; //FACADES
            case 2: return C2; //STREETS
//            case 3: return C3;
//            case 4: return C4;
//            case 5: return C5;
//            case 6: return C6;
//            case 7: return C7;
//            case 8: return C8;
//            case 9: return C9;
            default: return null;
        }
    }
}
