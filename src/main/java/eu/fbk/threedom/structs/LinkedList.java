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
package eu.fbk.threedom.structs;

/*

   HEAD
    1  ->  2  ->  3  ->  null

 */

public class LinkedList implements LinkedListInterface {

    private LlNode head;

    public LinkedList(){ }

    public LlNode head(){return head;}

    @Override
    public void addAtBeginning(Object o) {
        // create new node
        LlNode newNode = new LlNode(o);
        // set its next to actual head
        newNode.setNext(head);
        // set it as head
        head = newNode;
    }

    @Override
    public void addAtEnd(Object o) {
        // create new node
        LlNode newNode = new LlNode(o);
        // if ll is empty set newNode as head
        if(head == null)
            head = newNode;
        else {
            // traverse till ending node
            LlNode n = head;
            while (n.hasNext())
                n = n.next();
            // set new node as its next
            n.setNext(newNode);
        }
    }

    @Override
    public void remove(Object o){
        LlNode n = head;
        LlNode prev = null;
        while(n != null){ // traverse starting from head
            LlNode next = n.next();
            if(n.value().equals(o)){  // if we need to remove n
                if(n == head) { // if we are removing head
                    head = next;
                    n = null;
                }else {
                    prev.setNext(next); // set next of n prev
                }
            }else
                prev = n;

            n = next;
        }
    }

    @Override
    public void clear() { head = null; }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("llist [ ");

        LlNode n = head;
        if(head == null) sb.append("]");
        while(n != null){
            sb.append(n.value());

            if(n == head) sb.append("(head)");

            if(n.next() != null) {
                sb.append(" -> ");
                n = n.next();
            }else{
                sb.append(" ]");
                break;
            }
        }

        return sb.toString();
    }

    public static void main(String[]args){

        double a = 1.3;
        System.out.println((int)(a+0.5));
        a = 1.7;
        System.out.println((int)(a+0.5));
        System.exit(1);

        LinkedList ll = new LinkedList();

//        // add at beginning and print
//        ll.addAtBeginning("come");
//        ll.addAtBeginning("sono");
//        ll.addAtBeginning("bello");
//        System.out.println(ll.toString());
//
//        // clear
//        ll.clear();

        // add at end and print
        ll.addAtEnd("come");
        ll.addAtEnd("sono");
        ll.addAtEnd("bello");
        System.out.println(ll.toString());

        // remove center
//        ll.remove("sono");
//        System.out.println(ll.toString());

        // remove head
//        ll.remove("come");
//        System.out.println(ll.toString());

        // remove last
//        ll.remove("bello");
//        System.out.println(ll.toString());

        // remove all
//        ll.remove("bello");
//        ll.remove("sono");
//        ll.remove("come");
//        System.out.println(ll.toString());

        // remove from empty ll
//        ll.clear();
//        ll.remove("test");

        // remove all occurences
//        ll.clear();
//        ll.addAtEnd("come");
//        ll.addAtEnd("sono");
//        ll.addAtEnd("sono");
//        ll.addAtEnd("bello");
//
//        ll.remove("sono");
//        System.out.println(ll.toString());

        // remove all occurences till last
//        ll.clear();
//        ll.addAtEnd("come");
//        ll.addAtEnd("sono");
//        ll.addAtEnd("bello");
//        ll.addAtEnd("sono");
//
//        ll.remove("sono");
//        System.out.println(ll.toString());

        // remove all occurences from head
        ll.clear();
        ll.addAtEnd("come");
        ll.addAtEnd("sono");
        ll.addAtEnd("come");
        ll.addAtEnd("bello");

        ll.remove("come");
        System.out.println(ll.toString());
//        System.out.println(ll.head.toString());

        // iterate on the ll
        LlNode n = ll.head;
        while(n != null) {
            System.out.println(n.toString());

            if (!n.hasNext())
                break;
            n = n.next();
        }

    }
}
