package ru.ermakov.rssreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ru.ermakov.rssreader.fragments.SubscriptionsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.l_fragment_container, new SubscriptionsFragment())
                    .commit();
        }
    }
}
