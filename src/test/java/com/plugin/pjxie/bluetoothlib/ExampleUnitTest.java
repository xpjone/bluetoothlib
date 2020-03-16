package com.plugin.pjxie.bluetoothlib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println("hehe");
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testXianChen() {

        Hashtable hashtable = new Hashtable();
        ArrayList list = new ArrayList();


        new Thread(() -> {
            hashtable.put("key", "value");
            list.add(1);
        }
        ).start();

        new Thread(() -> {
            System.out.println(hashtable.size() + ":" + list.size());

        }
        ).start();


    }


}