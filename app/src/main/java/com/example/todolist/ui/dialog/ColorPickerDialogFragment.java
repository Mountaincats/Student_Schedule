package com.example.todolist.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.R;
import com.example.todolist.util.ColorPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ColorPickerDialogFragment extends DialogFragment {

    private static final String ARG_INITIAL_COLOR = "initial_color";
    private int tempColor;
    private List<Integer> savedColors = new ArrayList<>();
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public static ColorPickerDialogFragment newInstance(int initialColor) {
        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_COLOR, initialColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnColorSelectedListener) {
            listener = (OnColorSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnColorSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tempColor = getArguments().getInt(ARG_INITIAL_COLOR, 0xFF2196F3);
        }
        // 从 SharedPreferences 加载保存的颜色
        savedColors = new ArrayList<>(ColorPreferences.getSavedColors(requireContext()));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_color_picker, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews(dialogView, dialog);

        return dialog;
    }

    private void initViews(View view, AlertDialog dialog) {
        SeekBar sbRed = view.findViewById(R.id.sbRed);
        SeekBar sbGreen = view.findViewById(R.id.sbGreen);
        SeekBar sbBlue = view.findViewById(R.id.sbBlue);
        TextView tvRed = view.findViewById(R.id.tvRedValue);
        TextView tvGreen = view.findViewById(R.id.tvGreenValue);
        TextView tvBlue = view.findViewById(R.id.tvBlueValue);
        View previewView = view.findViewById(R.id.viewColorPreview);
        RecyclerView rvSaved = view.findViewById(R.id.rvSavedColors);
        Button btnConfirm = view.findViewById(R.id.btnConfirmColor);
        Button btnSave = view.findViewById(R.id.btnSaveColor);

        updateSeekBars(sbRed, sbGreen, sbBlue, tvRed, tvGreen, tvBlue, previewView, tempColor);

        SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int r = sbRed.getProgress();
                int g = sbGreen.getProgress();
                int b = sbBlue.getProgress();
                
                tvRed.setText(String.valueOf(r));
                tvGreen.setText(String.valueOf(g));
                tvBlue.setText(String.valueOf(b));
                
                previewView.setBackgroundColor(Color.rgb(r, g, b));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        sbRed.setOnSeekBarChangeListener(changeListener);
        sbGreen.setOnSeekBarChangeListener(changeListener);
        sbBlue.setOnSeekBarChangeListener(changeListener);

        SavedColorAdapter adapter = new SavedColorAdapter(savedColors, color -> {
            updateSeekBars(sbRed, sbGreen, sbBlue, tvRed, tvGreen, tvBlue, previewView, color);
        });
        rvSaved.setLayoutManager(new GridLayoutManager(getContext(), 5));
        rvSaved.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            int currentColor = Color.rgb(sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress());
            if (!savedColors.contains(currentColor)) {
                savedColors.add(0, currentColor);
                if (savedColors.size() > 20) savedColors.remove(savedColors.size() - 1);
                
                // 保存到 SharedPreferences
                ColorPreferences.saveColors(requireContext(), savedColors);
                
                adapter.notifyDataSetChanged();
            }
        });

        btnConfirm.setOnClickListener(v -> {
            int finalColor = Color.rgb(sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress());
            if (listener != null) {
                listener.onColorSelected(finalColor);
            }
            dismiss();
        });
    }

    private void updateSeekBars(SeekBar r, SeekBar g, SeekBar b, TextView tr, TextView tg, TextView tb, View preview, int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        r.setProgress(red);
        g.setProgress(green);
        b.setProgress(blue);
        
        tr.setText(String.valueOf(red));
        tg.setText(String.valueOf(green));
        tb.setText(String.valueOf(blue));
        
        preview.setBackgroundColor(color);
    }

    private static class SavedColorAdapter extends RecyclerView.Adapter<SavedColorAdapter.ColorViewHolder> {
        private List<Integer> colors;
        private OnColorClickListener listener;

        interface OnColorClickListener {
            void onColorClick(int color);
        }

        SavedColorAdapter(List<Integer> colors, OnColorClickListener listener) {
            this.colors = colors;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new View(parent.getContext());
            int size = dp2px(parent.getContext(), 40);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(size, size);
            int margin = dp2px(parent.getContext(), 8);
            params.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(params);
            return new ColorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
            int color = colors.get(position);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setStroke(dp2px(holder.itemView.getContext(), 1), 0xFFE0E0E0);
            
            holder.itemView.setBackground(drawable);
            holder.itemView.setOnClickListener(v -> listener.onColorClick(color));
        }

        @Override
        public int getItemCount() {
            return colors.size();
        }

        static class ColorViewHolder extends RecyclerView.ViewHolder {
            ColorViewHolder(View itemView) { super(itemView); }
        }
        
        private static int dp2px(Context context, float dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
        }
    }
}