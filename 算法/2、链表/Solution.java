package 链表;

/**
 * Definition for singly-linked list.
 * public class 链表.ListNode {
 *     int val;
 *     链表.ListNode next;
 *     链表.ListNode() {}
 *     链表.ListNode(int val) { this.val = val; }
 *     链表.ListNode(int val, 链表.ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public static ListNode deleteDuplicates(ListNode head) {
        if(head==null){
            return null;
        }

        //加入引用值
        ListNode cul=head;
        while(cul.next!=null){
            if(cul.val==cul.next.val){
                cul.next=cul.next.next;
            }else{
                cul=cul.next;
            }
        }

//        while(head.next!=null){
//            if(head.val==head.next.val){
//                head.next=head.next.next;
//            }else{
//                head=head.next;
//            }
//        }
        return head;
    }

    public static void main(String[] args) {
        ListNode head=new ListNode(0);
        head.next=new ListNode(1);
        head.next.next=new ListNode(1);
        head.next.next.next=new ListNode(2);


        ListNode listNode = deleteDuplicates(head);

        System.out.println(listNode);
    }
}