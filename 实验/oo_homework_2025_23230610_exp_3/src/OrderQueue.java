import com.oocourse.exp.Order;

import java.util.ArrayList;

public class OrderQueue {
    private final ArrayList<Order> orders = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Order order) {
        orders.add(order);
        // TODO: 唤醒在同一对象监视器上等待的所有线程
        notifyAll();
    }

    public synchronized Order poll() {
        // TODO: 从列表中取出第一个订单
        // HINT: 如果当前订单列表为空，且线程尚未结束，则在当前线程处等待，直到被其他线程唤醒
        // 根据TODO和HINT将foo和bar替换为合适的内容
        if (orders.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (orders.isEmpty()) {
            return null;
        }
        notifyAll();
        return orders.remove(0);
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return orders.isEmpty();
    }
}
