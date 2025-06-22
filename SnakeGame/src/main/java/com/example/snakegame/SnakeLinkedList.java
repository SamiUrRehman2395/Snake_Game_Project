package com.example.snakegame;



class SnakeNode {
    int x, y;
    SnakeNode next;

    public SnakeNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.next = null;
    }
}

public class SnakeLinkedList {
    private SnakeNode head;
    private SnakeNode tail;

    public SnakeLinkedList(int startX, int startY) {
        head = new SnakeNode(startX, startY);
        tail = head;
    }

    public SnakeNode getHead() {
        return head;
    }

    public void addFirst(int x, int y) {
        SnakeNode newHead = new SnakeNode(x, y);
        newHead.next = head;
        head = newHead;
        if (tail == null) {
            tail = head;
        }
    }

    public void removeLast() {
        if (head == null || head == tail) {
            head = tail = null;
            return;
        }
        SnakeNode current = head;
        while (current.next != null && current.next != tail) {
            current = current.next;
        }
        current.next = null;
        tail = current;
    }

    public boolean checkCollision(int x, int y) {
        SnakeNode current = head;
        while (current != null) {
            if (current.x == x && current.y == y) return true;
            current = current.next;
        }
        return false;
    }

    public void markOnGrid(char[][] grid) {
        SnakeNode current = head;
        boolean isHead = true;
        while (current != null) {
            if (current.y >= 0 && current.y < grid.length && current.x >= 0 && current.x < grid[0].length) {
                grid[current.y][current.x] = isHead ? 'H' : 'S'; // Head = 'H', Body = 'S'
            }
            current = current.next;
            isHead = false;
        }
    }
}


