package pl.itiner.grave;

import java.util.Date;
import java.util.GregorianCalendar;

import pl.itiner.commons.Commons;
import pl.itiner.db.NameHintProvider;
import pl.itiner.db.NameHintProvider.Columns;
import pl.itiner.db.NameHintProvider.QUERY_TYPES;
import pl.itiner.fetch.QueryParams;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FilterQueryProvider;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.common.base.Strings;

public class GFormFragment extends SherlockFragment {
	public static final String TAG = "GFormFragment";

	private static final int NONE_DATE = 3;
	private static final int BURIAL_DATE = 1;
	private static final int BIRTH_DATE = 2;
	private static final int DEATH_DATE = 0;

	private Spinner necropolis;
	private DatePicker datePicker;
	private CheckBox checkBoxDate;
	private AutoCompleteTextView editTextSurname;
	private AutoCompleteTextView editTextName;

	private int whichDate = NONE_DATE;

	private Button find;
	private RadioGroup dateGroup;

	private SearchActivity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof SearchActivity) {
			this.activity = (SearchActivity) activity;
		} else {
			throw new IllegalArgumentException("Activity is not instance of "
					+ SearchActivity.class.getSimpleName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.activity = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.search_form, container, false);

		find = (Button) root.findViewById(R.id.find_btn);
		setSearchClickListener();

		necropolis = (Spinner) root.findViewById(R.id.necropolis_spinner);
		necropolis.setAdapter(getNecropolisSpinnerAdapter());

		datePicker = (DatePicker) root.findViewById(R.id.datepicker);

		checkBoxDate = (CheckBox) root.findViewById(R.id.checkbox);
		checkBoxDate.setOnCheckedChangeListener(onCheckedDateVisiable);

		editTextSurname = (AutoCompleteTextView) root
				.findViewById(R.id.surname);
		editTextSurname.setSelected(false);
		editTextSurname.setAdapter(createAdapter(QUERY_TYPES.SURNAME));
		editTextSurname.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					search();
					InputMethodManager imm = (InputMethodManager) activity
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		editTextName = (AutoCompleteTextView) root.findViewById(R.id.name);
		editTextName.setSelected(false);
		editTextName.setAdapter(createAdapter(QUERY_TYPES.NAME));

		dateGroup = (RadioGroup) root.findViewById(R.id.dates_group);
		dateGroup.setOnCheckedChangeListener(onCheckDateType);
		return root;

	}

	@SuppressLint("DefaultLocale")
	private SimpleCursorAdapter createAdapter(
			final NameHintProvider.QUERY_TYPES type) {
		final int[] to = { android.R.id.text1 };
		final String[] from = { Columns.COLUMN_VALUE };
		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
				android.R.layout.simple_list_item_1, null, from, to,
				SimpleCursorAdapter.NO_SELECTION);

		adapter.setCursorToStringConverter(new CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor c) {
				return Commons.capitalizeFirstLetter(c.getString(c
						.getColumnIndex(Columns.COLUMN_VALUE)));
			}
		});

		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			@Override
			public Cursor runQuery(CharSequence constraint) {
				final String[] projection = { Columns._ID, Columns.COLUMN_VALUE };
				final String whereStatement = Columns.COLUMN_HINT_TYPE
						+ "=? AND " + Columns.COLUMN_VALUE + " LIKE ?";
				final String[] selectionArgs = { type.toString(),
						constraint.toString().toLowerCase().trim() + "%" };
				return activity.getContentResolver().query(
						NameHintProvider.CONTENT_URI, projection,
						whereStatement, selectionArgs, null);
			}
		});

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor c, int columnIndex) {
				String value = c.getString(columnIndex);
				((TextView) view).setText(Commons.capitalizeFirstLetter(value));
				return true;
			}
		});
		return adapter;
	}

	private OnCheckedChangeListener onCheckedDateVisiable = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				checkBoxDate.setText("");
				dateGroup.setVisibility(View.VISIBLE);
				datePicker.setVisibility(View.VISIBLE);
			} else {
				checkBoxDate.setText(R.string.additional_query_params);
				dateGroup.setVisibility(View.GONE);
				datePicker.setVisibility(View.GONE);
				whichDate = NONE_DATE;
			}
		}
	};

	private RadioGroup.OnCheckedChangeListener onCheckDateType = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.birth_date:
				whichDate = BIRTH_DATE;
				break;
			case R.id.death_date:
				whichDate = DEATH_DATE;
				break;
			case R.id.burial_date:
				whichDate = BURIAL_DATE;
				break;
			}
		}
	};

	private ArrayAdapter<CharSequence> getNecropolisSpinnerAdapter() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.necropolises,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	private void setSearchClickListener() {
		find.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search();
			}
		});
	}

	private void search() {
		editTextName.requestFocus();
		Long tmpNecropolisId = necropolis.getSelectedItemId() != 0 ? necropolis
				.getSelectedItemId() : null;
		Date deathDate = null;
		Date burialDate = null;
		Date birthDate = null;

		Date tmpDate = new GregorianCalendar(datePicker.getYear(),
				datePicker.getMonth(), datePicker.getDayOfMonth()).getTime();
		switch (whichDate) {
		case DEATH_DATE:
			deathDate = tmpDate;
			break;
		case BIRTH_DATE:
			birthDate = tmpDate;
			break;
		case BURIAL_DATE:
			burialDate = tmpDate;
			break;
		}
		final QueryParams params = new QueryParams(editTextName.getText()
				.toString(), editTextSurname.getText().toString(),
				tmpNecropolisId, birthDate, burialDate, deathDate);
		addQueryToCache(NameHintProvider.QUERY_TYPES.SURNAME, editTextSurname
				.getText().toString());
		addQueryToCache(NameHintProvider.QUERY_TYPES.NAME, editTextName
				.getText().toString());
		activity.search(params);
	}

	@SuppressLint("DefaultLocale")
	private void addQueryToCache(NameHintProvider.QUERY_TYPES type, String query) {
		if (!Strings.isNullOrEmpty(query)) {
			final String whereStatement = Columns.COLUMN_HINT_TYPE + "=? AND "
					+ Columns.COLUMN_VALUE + "=?";
			query = query.trim().toLowerCase();
			activity.getContentResolver().delete(NameHintProvider.CONTENT_URI,
					whereStatement, new String[] { type + "", query });
			ContentValues values = new ContentValues();
			values.put(Columns.COLUMN_HINT_TYPE, type + "");
			values.put(Columns.COLUMN_VALUE, query);
			activity.getContentResolver().insert(NameHintProvider.CONTENT_URI,
					values);
		}
	}

}
