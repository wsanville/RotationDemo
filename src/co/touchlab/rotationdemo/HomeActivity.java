package co.touchlab.rotationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Make sure to inherit from BaseActivity to get the functionality for retaining tasks.
 */
public class HomeActivity extends BaseActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.retain).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(HomeActivity.this, BasicRetainActivity.class));
            }
        });

        findViewById(R.id.loader).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(HomeActivity.this, LoaderActivity.class));
            }
        });

        findViewById(R.id.service).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(HomeActivity.this, ServiceActivity.class));
            }
        });
    }
}
