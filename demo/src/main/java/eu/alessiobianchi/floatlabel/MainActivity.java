package eu.alessiobianchi.floatlabel;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FloatLabel t = findViewById(R.id.txt1);
		t.setHintTextColor(Color.BLUE);
		t.reloadEditTextHintTextColor();

	}
}
