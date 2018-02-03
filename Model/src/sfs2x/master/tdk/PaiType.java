package sfs2x.master.tdk;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;

public class PaiType {
    public enum Type{
        invalid(0),//默认无效值
//        suit6_double6(-1),//六顺+双红六
//        fourKind_double6_suit3(-2),//双六+四条+三顺
//        fourKind_suit4(-3),//四条+四顺
//        suit4_double6_doubleK(-4),//双红6+双王+四顺
//        fourKind_set(-5),//四条+豹子
//        suit7(-6),//七顺
//        double6_suit4_set(-7),//双六+四顺+豹子
//        threeSet(-8),//双王+双红6+三顺、双红6+三顺+三条、双王+三顺+三条、双王+双红6+三条、三条+三条+三顺、三顺+三顺+三条
//        suit5_set(-9),//五顺加豹
//        suit6(-10),//六顺
//        suit4_set(-11),//四顺+双六或双王,四顺+三条,四顺+三顺
//        fourKind(-12),//四条
//        doubleSet(-13),//双豹
//        suit5(-14),//五顺
//        suit4(-15),//四顺
//        double6(-16),//双六
//        doubleK(-17),//双王
//        set(-18),//豹子
        fourKind(-1),//四条
        suit7(-2),//七顺
        pair6_pairK_set(-3),//俩6俩王+豹子或顺子
        suit5_pair6K(-4),//五顺加俩6或俩王
        double_set_suit(-5),//双豹带拐
        suit4_set(-6),//四顺加豹
        suit5_turn(-7),//五顺加拐
        suit6(-8),//六顺
        pair6K(-9),//俩6+俩王
        pair6_set(-10),//俩6+豹子或顺子
        pairK_set(-11),//俩王+豹子或顺子
        double_set(-12),//双顺,双豹,豹子+顺子
        suit4_turn(-13),//四顺加拐
        suit5(-14),//五顺
        suit3_set(-15),//三顺加豹
        suit4(-16),//四顺
        pair6(-17),//双6
        pairK(-18),//双王
        set(-19),//豹子或顺子
        high(-20);//散牌

        private int value;
        Type(int i) {
            value = i;
        }
        public int getValue(){
            return value;
        }
    }
    public static final int[] face = new int[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

    public Type type;
    public int[] arr;

    PaiType(Type type, int[] arr) {
        this.type = type;
        this.arr = arr;
    }


    private ArrayList<Integer> single = new ArrayList<>();//单张
    private ArrayList<Integer> pair = new ArrayList<>(); //对子
    private ArrayList<Integer> three = new ArrayList<>();//三条
    private ArrayList<Integer> four = new ArrayList<>();//四条
    private ArrayList<Integer> three_straight = new ArrayList<>();//三顺
    private ArrayList<Integer> four_straight = new ArrayList<>();//四顺
    private ArrayList<Integer> five_straight = new ArrayList<>();//五顺
    private ArrayList<Integer> six_straight = new ArrayList<>();//六顺
    private ArrayList<Integer> seven_straight = new ArrayList<>();//七顺

    private ArrayList<Integer> king = new ArrayList<>();
    private ArrayList<Integer> spade = new ArrayList<>();
    private ArrayList<Integer> heart = new ArrayList<>();
    private ArrayList<Integer> club = new ArrayList<>();
    private ArrayList<Integer> diamond = new ArrayList<>();

    private void analysisCard(ArrayList<Integer> card) {
        single.clear();
        pair.clear();
        three.clear();
        four.clear();
        three_straight.clear();
        four_straight.clear();
        five_straight.clear();
        six_straight.clear();
        seven_straight.clear();
        king.clear();
        spade.clear();
        heart.clear();
        club.clear();
        diamond.clear();
        Collections.sort(card);
        for (Integer aCard : card) {
            if (aCard / 4 < 15) {
                if (aCard % 4 == 0)
                    diamond.add(aCard);
                else if (aCard % 4 == 1)
                    club.add(aCard);
                else if (aCard % 4 == 2)
                    heart.add(aCard);
                else
                    spade.add(aCard);
            } else
                king.add(aCard);
        }
        for (int i = 0; i < card.size(); i++) {
            if (card.get(i) / 4 < 15) {

                ArrayList<Integer> n = new ArrayList<>();
                n.add(card.get(i));
                for (int j = i + 1; j <= i + 3; j++) {
                    if (j < card.size()) {
                        if (card.get(j) / 4 == card.get(j - 1) / 4) {
                            n.add(card.get(j));
                            i = j;
                        } else {
                            i = j - 1;
                            break;
                        }
                    }
                }
                if (n.size() == 1)
                    single.addAll(n);
                else if (n.size() == 2)
                    pair.addAll(n);
                else if (n.size() == 3)
                    three.addAll(n);
                else
                    four.addAll(n);
            }
        }
        if (king.size() == 1)
            single.addAll(king);
        else if (king.size() == 2)
            pair.addAll(king);
        analysisStraightFlush(spade);
        analysisStraightFlush(heart);
        analysisStraightFlush(club);
        analysisStraightFlush(diamond);
    }

    private void analysisStraightFlush(ArrayList<Integer> color) {
        if (color.size() < 3)
            return;
        ArrayList<Integer> n = new ArrayList<>();
        n.add(color.get(0));
        for (int i=1;i<color.size();i++){
            if (color.get(i)/4 == color.get(i-1)/4+1){
                n.add(color.get(i));
            }
            if (color.get(i)/4 != color.get(i-1)/4+1 || i == color.size() -1){
                if (n.size() == 3){
                    three_straight.addAll(n);
                }else if (n.size() == 4)
                    four_straight.addAll(n);
                else if (n.size() == 5)
                    five_straight.addAll(n);
                else if (n.size() == 6)
                    six_straight.addAll(n);
                else if (n.size() == 7)
                    seven_straight.addAll(n);
                n.clear();
                n.add(color.get(i));
            }
        }
//        if (color.size() >= 3) {
//            for (int i = 0; i + 2 < color.size(); i++) {
//                ArrayList<Integer> n = new ArrayList<>();
//                n.add(color.get(i));
//                for (int j = i + 1; j <= i + 2; j++) {
//                    if (color.get(j) / 4 == color.get(j - 1) / 4 + 1)
//                        n.add(color.get(j));
//                }
//                if (n.size() == 3)
//                    three_straight.addAll(n);
//            }
//        }
//        if (color.size() >= 4) {
//            for (int i = 0; i + 3 < color.size(); i++) {
//                ArrayList<Integer> n = new ArrayList<>();
//                n.add(color.get(i));
//                for (int j = i + 1; j <= i + 3; j++) {
//                    if (color.get(j) / 4 == color.get(j - 1) / 4 + 1)
//                        n.add(color.get(j));
//                }
//                if (n.size() == 4)
//                    four_straight.addAll(n);
//            }
//        }
//        if (color.size() >= 5) {
//            for (int i = 0; i + 4 < color.size(); i++) {
//                ArrayList<Integer> n = new ArrayList<>();
//                n.add(color.get(i));
//                for (int j = i + 1; j <= i + 4; j++) {
//                    if (color.get(j) / 4 == color.get(j - 1) / 4 + 1)
//                        n.add(color.get(j));
//                }
//                if (n.size() == 5)
//                    five_straight.addAll(n);
//            }
//        }
//        if (color.size() >= 6) {
//            for (int i = 0; i + 5 < color.size(); i++) {
//                ArrayList<Integer> n = new ArrayList<>();
//                n.add(color.get(i));
//                for (int j = i + 1; j <= i + 5; j++) {
//                    if (color.get(j) / 4 == color.get(j - 1) / 4 + 1)
//                        n.add(color.get(j));
//                }
//                if (n.size() == 6)
//                    six_straight.addAll(n);
//            }
//        }
//        if (color.size() >= 7) {
//            for (int i = 0; i + 6 < color.size(); i++) {
//                ArrayList<Integer> n = new ArrayList<>();
//                n.add(color.get(i));
//                for (int j = i + 1; j <= i + 6; j++) {
//                    if (color.get(j) / 4 == color.get(j - 1) / 4 + 1)
//                        n.add(color.get(j));
//                }
//                if (n.size() == 7)
//                    seven_straight.addAll(n);
//            }
//        }
    }


    public void getType(ArrayList<Integer> h) {
        if (h == null)
            return;
        ArrayList<Integer> hand = new ArrayList<>(h);
        Iterator<Integer> it = hand.iterator();
        while (it.hasNext()){
            int i = it.next();
            if (i == -1)
                it.remove();
        }
        if (hand.size() == 0 || hand.size() > 7)
            return;
        Collections.sort(hand);
       analysisCard(hand);

        //四条
        if (four.size() /4 == 1){
            type = Type.fourKind;
            arr = new int[]{four.get(0)/4};
            return;
        }
        //七顺
        if (hand.size() == 7) {
            if (seven_straight.size()/7 == 0) {
                type = Type.suit7;
                arr = new int[]{seven_straight.get(6) / 4};
                return;
            }
        }
        //双6,双王,豹子或顺子
        if (hand.size() == 7){
            if (pair.size() > 0 && pair.get(0)/4 == 6 && pair.get(pair.size() - 1)/4 > 14){
                ArrayList<Integer> temp = new ArrayList<>(hand);
                Iterator<Integer> iterator = temp.iterator();
                while (iterator.hasNext()){
                    int n = iterator.next()/4;
                    if (n== 6 || n > 14)
                        iterator.remove();
                }
                PaiType t = new PaiType(Type.invalid,null);
                t.analysisCard(temp);
                if (t.three.size() / 3== 1 || t.three_straight.size() / 3 == 1){
                    type = Type.pair6_pairK_set;
                    arr = new int[]{0};
                    return;
                }
            }
        }
        //五顺加俩6或俩王
        if (hand.size() == 7){
            if (pair.size() > 0){
                if (pair.get(0)/4 == 6){
                    ArrayList<Integer> temp = new ArrayList<>(hand);
                    Iterator<Integer> iterator = temp.iterator();
                    while (iterator.hasNext()){
                        int n = iterator.next()/4;
                        if (n== 6)
                            iterator.remove();
                    }
                    PaiType t = new PaiType(Type.invalid,null);
                    t.analysisCard(temp);
                    if (t.five_straight.size() / 5 == 1){
                        type = Type.suit5_pair6K;
                        arr = new int[]{t.five_straight.get(4)/4,16};
                        return;
                    }
                }
                if (pair.get(pair.size() - 1)/4 > 14){
                    ArrayList<Integer> temp = new ArrayList<>(hand);
                    Iterator<Integer> iterator = temp.iterator();
                    while (iterator.hasNext()){
                        int n = iterator.next()/4;
                        if (n > 14)
                            iterator.remove();
                    }
                    PaiType t = new PaiType(Type.invalid,null);
                    t.analysisCard(temp);
                    if (t.five_straight.size() / 5 == 1){
                        type = Type.suit5_pair6K;
                        arr = new int[]{t.five_straight.get(4)/4,15};
                        return;
                    }
                }
            }
        }
        //双豹带拐
        if (hand.size() == 7){
            if (three.size()/3 == 2 && three_straight.size()/3 == 1 ){
                type = Type.double_set_suit;
                arr = new int[]{three_straight.get(1)/4};
//                Integer[] a = ArrayUtils.toObject(arr);
//                Arrays.sort(a,Collections.reverseOrder());
//                arr = ArrayUtils.toPrimitive(a);
                return;
            }
            if (three.size()/3 == 1 && three_straight.size()/3 == 2){
                type = Type.double_set_suit;
                arr = new int[]{three_straight.get(1)/4};
//                Integer[] a = ArrayUtils.toObject(arr);
//                Arrays.sort(a,Collections.reverseOrder());
//                arr = ArrayUtils.toPrimitive(a);
                return;
            }
        }
        //四顺加豹
        if (hand.size() >= 6){
            if (three.size()/3 == 1){
                ArrayList<Integer> temp = new ArrayList<>(hand);
                Iterator<Integer> iterator = temp.iterator();
                while (iterator.hasNext()){
                    int n = iterator.next()/4;
                    if (n == three.get(0)/4)
                        iterator.remove();
                }
                PaiType t = new PaiType(Type.invalid,null);
                t.analysisCard(temp);
                if (t.four_straight.size() / 4 == 1){
                    type = Type.suit4_set;
                    arr = new int[]{t.four_straight.get(3)/4,three.get(0)};
                    return;
                }
            }
            if (pair.size() > 0){
                if (pair.get(0)/4 == 6){
                    ArrayList<Integer> temp = new ArrayList<>(hand);
                    Iterator<Integer> iterator = temp.iterator();
                    while (iterator.hasNext()){
                        int n = iterator.next()/4;
                        if (n == 6)
                            iterator.remove();
                    }
                    PaiType t = new PaiType(Type.invalid,null);
                    t.analysisCard(temp);
                    if (t.four_straight.size() / 4 == 1){
                        type = Type.suit4_set;
                        arr = new int[]{t.four_straight.get(3)/4,16};
                        return;
                    }
                }
                if (pair.get(pair.size() - 1)/4 > 14){
                    ArrayList<Integer> temp = new ArrayList<>(hand);
                    Iterator<Integer> iterator = temp.iterator();
                    while (iterator.hasNext()){
                        int n = iterator.next()/4;
                        if (n > 14)
                            iterator.remove();
                    }
                    PaiType t = new PaiType(Type.invalid,null);
                    t.analysisCard(temp);
                    if (t.four_straight.size() / 4 == 1){
                        type = Type.suit4_set;
                        arr = new int[]{t.four_straight.get(3)/4,15};
                        return;
                    }
                }
            }
        }
        //五顺加拐
        if (hand.size() == 7){
            if (five_straight.size()/5 == 1 && three.size()/3 == 1){
                type = Type.suit5_turn;
                arr = new int[]{five_straight.get(4)/4};
                return;
            }
        }
        //六顺
        if (hand.size() >= 6){
            if (six_straight.size()/6 == 1){
                type = Type.suit6;
                arr = new int[]{six_straight.get(5)/4};
                return;
            }
        }
        //双6+双王
        if (hand.size() > 4 && pair.size() > 4){
            if (pair.get(0)/4 == 0 && pair.get(pair.size() - 1)/4 > 14){
                type = Type.pair6K;
                arr = new int[]{0};
                return;
            }
        }
        //双6+豹子或顺子
        if (hand.size() >= 5){
            if (pair.size() > 0){
                if (pair.get(0)/4 == 6){
                    ArrayList<Integer> temp = new ArrayList<>(hand);
                    Iterator<Integer> iterator = temp.iterator();
                    while (iterator.hasNext()){
                        int n = iterator.next()/4;
                        if (n == 6)
                            iterator.remove();
                    }
                    PaiType t = new PaiType(Type.invalid,null);
                    t.analysisCard(temp);
                    if (t.three_straight.size() / 4 == 1){
                        type = Type.pair6_set;
                        arr = new int[]{0};
                        return;
                    }
                }
            }
        }
        //双王+豹子或顺子
        if (hand.size() >= 5){
            if (pair.size() > 0){
                if (pair.get(pair.size() - 1)/4 > 14){
                    ArrayList<Integer> temp = new ArrayList<>(hand);
                    Iterator<Integer> iterator = temp.iterator();
                    while (iterator.hasNext()){
                        int n = iterator.next()/4;
                        if (n > 14)
                            iterator.remove();
                    }
                    PaiType t = new PaiType(Type.invalid,null);
                    t.analysisCard(temp);
                    if (t.three_straight.size() / 4 == 1){
                        type = Type.pairK_set;
                        arr = new int[]{0};
                        return;
                    }
                }
            }
        }
        //双顺,双豹,豹子+顺子
        if (hand.size() >= 6){
            if (three.size()/3 == 2){
                type = Type.double_set;
                arr = new int[]{three.get(3)/4,three.get(0)/4};
                return;
            }
            if (three_straight.size() / 3 == )
        }
        //双王+双红6+三顺、双红6+三顺+三条、双王+三顺+三条、双王+双红6+三条、三条+三条+三顺、三顺+三顺+三条
        if (hand.size() == 7) {
            if (pair.size() > 0) {
                //双6+双王+
                if ( pair.size()/2 >= 2 && pair.get(0) / 4 == 6 && pair.get(pair.size() - 2) / 4 > 14) {
                    if (three.size() / 3 == 1) {
                        type = Type.threeSet;
                        arr = new int[]{16, 15, three.get(0) / 4};
                        return;
                    }
                    if (three_straight.size() / 3 == 1) {
                        type = Type.threeSet;
                        arr = new int[]{16, 15, three_straight.get(1) / 4};
                        return;
                    }
                }
                //双6+
                if (pair.get(0)/4==6){
                    if (three.size()/3 == 2){
                        type = Type.threeSet;
                        arr = new int[]{16,three.get(3)/4,three.get(0)/4};
                        return;
                    }
                    if (three_straight.size()/3 == 2){
                        type = Type.threeSet;
                        if (three_straight.get(1)/4 > three_straight.get(4)/4)
                            arr = new int[]{16,three_straight.get(1)/4,three_straight.get(4)/4};
                        else
                            arr = new int[]{16,three_straight.get(4)/4,three_straight.get(1)/4};
                        return;
                    }
                    if (three.size()/3==1 && three_straight.size()/3 == 1){
                        type = Type.threeSet;
                        if (three.get(0)/4 > three_straight.get(1)/4)
                            arr = new int[]{16,three.get(0)/4,three_straight.get(1)/4};
                        else
                            arr = new int[]{16,three_straight.get(1)/4,three.get(0)/4};
                        return;
                    }
                }
                //双王+
                if (pair.get(pair.size()-2)/4>14){
                    if (three.size()/3 == 2){
                        type = Type.threeSet;
                        arr = new int[]{15,three.get(3)/4,three.get(0)/4};
                        return;
                    }
                    if (three_straight.size()/3 == 2){
                        type = Type.threeSet;
                        if (three_straight.get(1)/4 > three_straight.get(4)/4)
                            arr = new int[]{15,three_straight.get(1)/4,three_straight.get(4)/4};
                        else
                            arr = new int[]{15,three_straight.get(4)/4,three_straight.get(1)/4};
                        return;
                    }
                    if (three.size()/3==1 && three_straight.size()/3 == 1){
                        type = Type.threeSet;
                        if (three.get(0)/4 > three_straight.get(1)/4)
                            arr = new int[]{15,three.get(0)/4,three_straight.get(1)/4};
                        else
                            arr = new int[]{15,three_straight.get(1)/4,three.get(0)/4};
                        return;
                    }
                }
            }
            //三条+三条+三顺
            if (three.size()/3  == 2 && three_straight.size()/3 == 1){
                type = Type.threeSet;
                arr = new int[]{three.get(0)/4,three.get(3)/4,three_straight.get(1)/4};
                Integer[] a = ArrayUtils.toObject(arr);
                Arrays.sort(a,Collections.reverseOrder());
                arr = ArrayUtils.toPrimitive(a);
                return;
            }
            //三顺+三顺+三条
            if (three.size()/3 == 1 && three_straight.size()/3 == 2){
                type = Type.threeSet;
                arr = new int[]{three.get(0)/4,three_straight.get(1)/4,three_straight.get(4)/4};
                Integer[] a = ArrayUtils.toObject(arr);
                Arrays.sort(a,Collections.reverseOrder());
                arr = ArrayUtils.toPrimitive(a);
                return;
            }
        }
        //五顺+双红6、五顺+双王、五顺+三条
        if (hand.size() == 7) {
            if (five_straight.size()/5==1){
                if (pair.size() > 0){
                    if (pair.get(0)/4 == 6){
                        type = Type.suit5_set;
                        arr = new int[]{five_straight.get(4)/4,16};
                        return;
                    }
                    if (pair.get(pair.size()-2)/4>14){
                        type = Type.suit5_set;
                        arr = new int[]{five_straight.get(4)/4,15};
                        return;
                    }
                }
                if (three.size()/3 == 1){
                    type = Type.suit5_set;
                    arr = new int[]{five_straight.get(4)/4,three.get(0)/4};
                    return;
                }
            }
        }
        //六顺
        if (six_straight.size() / 6 == 1) {
            type = Type.suit6;
            arr = new int[]{six_straight.get(5) / 4};
            return;
        }

        //四顺+双六或双王,四顺+三条,四顺+三顺
        if (four_straight.size()/4==1){
            if (pair.size() > 0){
                if (pair.get(0)/4 == 6){
                    type = Type.suit4_set;
                    arr = new int[]{four_straight.get(3)/4,16};
                    return;
                }
                if (pair.get(pair.size()-2)/4>14){
                    type = Type.suit4_set;
                    arr = new int[]{four_straight.get(3)/4,15};
                    return;
                }
            }
            if (three.size()/3==1){
                type = Type.suit4_set;
                arr = new int[]{four_straight.get(3)/4,three.get(0)/4};
                return;
            }
            if (three_straight.size()/3==1){
                type = Type.suit4_set;
                arr = new int[]{four_straight.get(3)/4,three_straight.get(1)/4};
                return;
            }
        }
        //四条
        if (four.size()/4 == 1){
            type = Type.fourKind;
            arr = new int[]{four.get(0)/4};
            return;
        }
        //双红6+双王、双三条（如法777999）、三条+三顺（如888JQK）、双三顺（如789JQK）、双红6+三条、双红6+三顺、双王+三条、双王+三顺
        //双6+双王,三条,三顺
        if (pair.size()>0){
            if (pair.size() >= 4 && pair.get(0)/4==6 && pair.get(pair.size()-2)/4>14){//双王
                type = Type.doubleSet;
                arr = new int[]{16,15};
                return;
            }
            //双6
            if (pair.get(0)/4==6){
                //三条
                if (three.size()/3 == 1){
                    type = Type.doubleSet;
                    arr = new int[]{16,three.get(0)/4};
                    return;
                }
                //三顺
                if (three_straight.size() / 3 == 1){
                    type = Type.doubleSet;
                    arr = new int[]{16,three_straight.get(1)/4};
                    return;
                }
            }
            if (pair.get(pair.size()-2)/4 >14) { //双王+三条,三顺
                if (three.size() / 3 == 1) {//三条
                    type = Type.doubleSet;
                    arr = new int[]{15, three.get(0) / 4};
                    return;
                }
                if (three_straight.size() / 3 == 1) {//三顺
                    type = Type.doubleSet;
                    arr = new int[]{15, three_straight.get(1) / 4};
                    return;
                }
            }
        }
        //双三条
        if (three.size()/3 == 2){
            type = Type.doubleSet;
            arr = new int[]{three.get(3)/4,three.get(0)/4};
            return;
        }
        //三条+三顺
        if (three.size()/3 == 1 && three_straight.size()/3 == 1){
            type = Type.doubleSet;
            if (three.get(0)/4 > three_straight.get(1)/4)
                arr = new int[]{three.get(0)/4,three_straight.get(1)/4};
            else
                arr = new int[]{three_straight.get(1)/4,three.get(0)/4};
            return;
        }
        //双三顺
        if (three_straight.size()/3 == 2){
            type = Type.doubleSet;
            if (three_straight.get(1)/4 > three_straight.get(4)/4)
                arr = new int[]{three_straight.get(1)/4,three_straight.get(4)/4};
            else
                arr = new int[]{three_straight.get(4)/4,three_straight.get(1)/4};
            return;
        }
        //五顺
        if (five_straight.size()/5 == 1){
            type = Type.suit5;
            arr = new int[]{five_straight.get(4)/4};
            return;
        }
        //四顺
        if (four_straight.size()/4 == 1){
            type = Type.suit4;
            arr = new int[]{four_straight.get(3)/4};
            return;
        }
        //双6
        if (pair.size()>0 && pair.get(0)/4==6){
            type = Type.double6;
            arr = new int[]{0};
            return;
        }
        //双王
        if (pair.size() > 0 && pair.get(pair.size() - 2)/4 > 14){
            type = Type.doubleK;
            arr = new int[]{0};
            return;
        }
        //豹子
        if (three.size()/3==1){//三条
            type = Type.set;
            arr = new int[]{three.get(0)/4};
            return;
        }
        if (three_straight.size()/3==1){//三顺
            type = Type.set;
            arr = new int[]{three_straight.get(1)/4};
            return;
        }
        type = Type.high;
        int n = 0;
        for (int i:hand){
            n+=i/4;
        }
        arr = new int[]{n};
    }


    public static void main(String[] args) {
        PaiType type = new PaiType(Type.invalid ,new int[]{0});
        Map<Integer,ArrayList<Integer>> pokers = new HashMap<>();
        ArrayList<Integer> pk = new ArrayList<>();
        ArrayList<Integer> face = new ArrayList<>();
        for (int f : PaiType.face) {
            if (f == 6) {
                if (pokers.get(f) == null)
                    pokers.put(f,new ArrayList<Integer>());
                pokers.get(f).add(f*4);
                pokers.get(f).add(f*4+2);
                pk.add(f*4);
                pk.add(f*4+2);
            } else if (f == 15 || f == 16) {//大小王
                if (pokers.get(15) == null)
                    pokers.put(15,new ArrayList<Integer>());
                pokers.get(15).add(f*4);
                pk.add(f*4);
            } else {
                if (pokers.get(f) == null)
                    pokers.put(f,new ArrayList<Integer>());
                pokers.get(f).add(f*4);
                pokers.get(f).add(f*4+1);
                pokers.get(f).add(f*4+2);
                pokers.get(f).add(f*4+3);
                pk.add(f*4);
                pk.add(f*4+1);
                pk.add(f*4+2);
                pk.add(f*4+3);
            }
        }

        for (int i = 0; i < pk.size(); i++) {
            int k = RandomUtils.nextInt(pk.size());
            int temp = pk.get(i);
            pk.set(i, pk.get(k));
            pk.set(k, temp);
        }

        ArrayList<Integer> cards = new ArrayList<>();
//        while (true){
//            int n = RandomUtils.nextInt(pokers.size());
//            if (pokers.get(6+n).size() == 4) {
//                cards.addAll(pokers.get(6 + n));
//                pokers.get(6+n).clear();
//                break;
//            }
//        }
//        while (true){
//            int n = RandomUtils.nextInt(pokers.size());
//            if (pokers.get(6+n).size() > 0){
//                int i = RandomUtils.nextInt(pokers.get(6+n).size());
//                cards.add(pokers.get(6+n).remove(i));
//                break;
//            }
//        }
        cards.add(10*4+2);
        cards.add(11*4);
        cards.add(9*4);
        cards.add(10*4);
        cards.add(13*4+1);
        cards.add(10*4+1);
        cards.add(8*4);
//        for (int i=0;i<7;i++){
//            int n = pk.remove(0);
//            cards.add(n);
//        }

        for (int n:cards){
            face.add(n/4);
        }
        Collections.sort(face);
        //手牌
//        for (int i = 0; i < 6; i++) {
//            int n = (6 + i) * 4;
//            pokers.remove(Integer.valueOf(n));
//            cards.add(n);
//            face.add(n / 4);
//        }
//        int n = 6 * 4 + 2;
//        pokers.remove(Integer.valueOf(n));
//        cards.add(n);
//        face.add(n / 4);
//

        long start = System.currentTimeMillis();
        type.getType(cards);
        System.out.println("耗时:"+(System.currentTimeMillis() - start));

        System.out.println("手牌:" + cards.toString());
        System.out.println("face:" + face.toString());
        System.out.println("手牌数:"+cards.size());
        System.out.println("\n");
        System.out.println("黑桃:" + type.spade.toString());
        System.out.println("红桃:" + type.heart.toString());
        System.out.println("梅花:" + type.club.toString());
        System.out.println("方块:" + type.diamond.toString());
        System.out.println("\n");
        System.out.println("单张:" + type.single.toString());
        System.out.println("对子:" + type.pair.toString());
        System.out.println("三条:" + type.three.toString());
        System.out.println("四条:" + type.four.toString());
        System.out.println("\n");
        System.out.println("三顺:" + type.three_straight.toString());
        System.out.println("四顺:" + type.four_straight.toString());
        System.out.println("五顺:" + type.five_straight.toString());
        System.out.println("六顺:" + type.six_straight.toString());
        System.out.println("七顺:" + type.seven_straight.toString());
        System.out.println("\n");
        System.out.println("牌型:"+type.type);
        System.out.println("arr:"+Arrays.toString(type.arr));
    }
}
