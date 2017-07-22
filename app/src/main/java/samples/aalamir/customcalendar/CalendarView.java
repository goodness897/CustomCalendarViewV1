package samples.aalamir.customcalendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by a7med on 28/06/2015.
 */
public class CalendarView extends LinearLayout {
    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // date format
    private String dateFormat = "yyyy년 MM월";

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView tvDate;
    private GridView grid;

    private Context context;
    private ArrayList<Date> cells;
    private Date selectDate;

    public CalendarView(Context context) {
        super(context);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        setView();
        setClickListener();

        updateCalendar();
    }

    private void setView() {
        // layout is inflated, assign local variables to components
        header = (LinearLayout) findViewById(R.id.calendar_header);
        btnPrev = (ImageView) findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView) findViewById(R.id.calendar_next_button);
        tvDate = (TextView) findViewById(R.id.calendar_date_display);
        grid = (GridView) findViewById(R.id.calendar_grid);
    }

    private void setClickListener() {
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });
    }


    public void updateCalendar() {
        updateCalendar(null);
    }


    public void updateCalendar(HashSet<Date> events) {
        cells = new ArrayList<>();
        final Calendar calendar = (Calendar) currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 2;

        // move calendar backwards to the beginning of the week
        if (monthBeginningCell == -1) {
            monthBeginningCell = 6;
        }
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        final CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), cells, events);
        grid.setAdapter(calendarAdapter);
        calendarAdapter.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectDate = cells.get(position);
                calendarAdapter.notifyDataSetChanged();
            }
        });

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Calendar todayCalendar = Calendar.getInstance();
        if (todayCalendar.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) && todayCalendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) {
            btnNext.setVisibility(GONE);
        } else {
            btnNext.setVisibility(VISIBLE);
        }
        tvDate.setText(sdf.format(currentDate.getTime()));
    }


    private class CalendarAdapter extends ArrayAdapter<Date> {
        // days with events
        private HashSet<Date> holidays;

        // for view inflation
        private LayoutInflater inflater;

        private OnDateClickListener onDateClickListener;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> holidays) {
            super(context, R.layout.control_calendar_day, days);
            this.holidays = holidays;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            // day in question
            final Date date = getItem(position);
            Calendar calDate = Calendar.getInstance();
            calDate.setTime(date);
            int day = calDate.get(Calendar.DAY_OF_MONTH);
            final int month = calDate.get(Calendar.MONTH);
            final int year = calDate.get(Calendar.YEAR);

            // today
            final Date today = new Date();
            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            if (view == null)
                view = inflater.inflate(R.layout.control_calendar_day, parent, false);

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onDateClickListener != null) {
                        if (!(month != currentDate.get(Calendar.MONTH) || year != currentDate.get(Calendar.YEAR) || date.after(today))) {
                            onDateClickListener.onClick(view, position);
                        }
                    }
                }
            });
            if (holidays != null) {
                for (Date eventDate : holidays) {
                    if (eventDate.getDate() == day &&
                            eventDate.getMonth() == month &&
                            eventDate.getYear() == year) {
                        ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.calendar_sunday_select_color));
                        break;
                    }
                }
            }

            ((TextView) view).setTypeface(null, Typeface.NORMAL);
            view.setBackgroundResource(0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // 글자 색 세팅(토요일, 일요일, 그냥)
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.calendar_saturday_select_color));
            } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.calendar_sunday_select_color));
            } else {
                ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.calendar_select_color));
            }


            //알파값 세팅(오늘 이후 alpha 50%)
            if (calDate.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) && calDate.after(calToday)) {
                view.setAlpha(0.5f);
            } else {
                view.setAlpha(1);
            }

            if (month != currentDate.get(Calendar.MONTH) || year != currentDate.get(Calendar.YEAR)) {
                ((TextView) view).setText("");
            } else if (day == calToday.get(Calendar.DATE) && month == calToday.get(Calendar.MONTH) && year == calToday.get(Calendar.YEAR)) {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_today));
                ((TextView) view).setText(String.valueOf(calDate.get(Calendar.DATE)));
            } else {
                ((TextView) view).setText(String.valueOf(calDate.get(Calendar.DATE)));
            }

            // 선택된 날짜일때
            if (selectDate == date) {
                ((TextView) view).setTextColor(Color.parseColor("#ffffff"));
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_select_day));
                ((TextView) view).setText(String.valueOf(calDate.get(Calendar.DATE)));

            }

            return view;
        }

        public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
            this.onDateClickListener = onDateClickListener;
        }


    }

    public interface OnDateClickListener {
        void onClick(View view, int position);

    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler {
        void onDayLongPress(Date date);
    }

}
