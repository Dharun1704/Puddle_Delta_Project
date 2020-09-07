package com.example.puddle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingsBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener bListener;
    private TextView uWind, uPress, uVis;
    private LinearLayout wind, pressure, visibility, wtrStg;
    private Button doneButton, resetButton;
    private ArrayAdapter<String> unit;
    private SwitchCompat tempSwitch;

    String[] fUnits;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.weather_settings, container, false);
        wtrStg = v.findViewById(R.id.wtr_stg);
        wind = v.findViewById(R.id.wind_settings);
        tempSwitch = v.findViewById(R.id.temp_switch);
        pressure = v.findViewById(R.id.pressure_settings);
        visibility = v.findViewById(R.id.visibility_settings);
        uWind = v.findViewById(R.id.wind_unit);
        uPress = v.findViewById(R.id.pressure_unit);
        uVis = v.findViewById(R.id.visibility_unit);
        doneButton = v.findViewById(R.id.btn_done);
        resetButton = v.findViewById(R.id.btn_reset);

        fUnits = new String[4];
        SharedPreferences tUNIT = requireActivity().getSharedPreferences("TemperatureUnit", Context.MODE_PRIVATE);
        fUnits[0] = tUNIT.getString("tempUnit", "C");
        SharedPreferences wUNIT = requireActivity().getSharedPreferences("WindUnit", Context.MODE_PRIVATE);
        fUnits[1] = wUNIT.getString("windUnit", "Kilometer per hour - km/h");
        SharedPreferences pUNIT = requireActivity().getSharedPreferences("PressureUnit", Context.MODE_PRIVATE);
        fUnits[2] = pUNIT.getString("pressUnit", "Hectopascals - hPa");
        SharedPreferences vUNIT = requireActivity().getSharedPreferences("VisibilityUnit", Context.MODE_PRIVATE);
        fUnits[3] = vUNIT.getString("visUnit", "Kilometer - km");

        if (fUnits[0].equals("C")) {
            tempSwitch.setChecked(false);
        }
        else if (fUnits[0].equals("F")) {
            tempSwitch.setChecked(true);
        }
        uWind.setText(fUnits[1]);
        uPress.setText(fUnits[2]);
        uVis.setText(fUnits[3]);

        Drawable unwrapped = AppCompatResources.getDrawable(requireContext(), R.drawable.background3);
        assert unwrapped != null;
        Drawable wrapped = DrawableCompat.wrap(unwrapped);
        GradientDrawable drawable = (GradientDrawable) wrapped;
        drawable.setCornerRadius(10f);
        doneButton.setBackground(drawable);
        resetButton.setBackground(drawable);

        tempSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    fUnits[0] = "C";

                    SharedPreferences tUNIT = requireActivity().getSharedPreferences("TemperatureUnit", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorT = tUNIT.edit();
                    editorT.putString("tempUnit", fUnits[0]);
                    editorT.apply();
                }

                else {
                    fUnits[0] = "F";

                    SharedPreferences tUNIT = requireActivity().getSharedPreferences("TemperatureUnit", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorT = tUNIT.edit();
                    editorT.putString("tempUnit", fUnits[0]);
                    editorT.apply();
                }
            }
        });

        wind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] selected = {0};

                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                selected[0] = Selected.getInt("OptionSelectedWind", 0);

                unit = new ArrayAdapter<>(requireActivity(), R.layout.dialog_item);
                unit.add("Kilometers per hour - km/h");
                unit.add("Miles per hour - mph");
                unit.add("Nautical miles per hour - kts");

                new AlertDialog.Builder(getContext())
                        .setTitle("Select Wind Unit")
                        .setSingleChoiceItems(unit, selected[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected[0] = which;

                                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = Selected.edit();
                                editor.putInt("OptionSelectedWind", selected[0]);
                                editor.apply();
                            }
                        })
                        .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                fUnits[1] = unit.getItem(selected[0]);

                                SharedPreferences wUNIT = requireActivity().getSharedPreferences("WindUnit", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = wUNIT.edit();
                                editor.putString("windUnit", fUnits[1]);
                                editor.apply();

                                uWind.setText(fUnits[1]);
                            }
                        })
                        .show();
            }
        });

        pressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] selected = {0};

                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                selected[0] = Selected.getInt("OptionSelectedPress", 0);

                unit = null;
                unit = new ArrayAdapter<>(requireActivity(), R.layout.dialog_item);
                unit.add("HectoPascals - hPa");
                unit.add("Millimeters of Mercury - mmHg");
                unit.add("Inches of Mercury - inHg");

                new AlertDialog.Builder(getContext())
                        .setTitle("Select Pressure Unit")
                        .setSingleChoiceItems(unit, selected[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected[0] = which;

                                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = Selected.edit();
                                editor.putInt("OptionSelectedPress", selected[0]);
                                editor.apply();
                            }
                        })
                        .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                fUnits[2] = unit.getItem(selected[0]);

                                SharedPreferences pUNIT = requireActivity().getSharedPreferences("PressureUnit", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pUNIT.edit();
                                editor.putString("pressUnit", fUnits[2]);
                                editor.apply();

                                uPress.setText(fUnits[2]);
                            }
                        })
                        .show();
            }
        });

        visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] selected = {0};

                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                selected[0] = Selected.getInt("OptionSelectedVis", 0);

                unit = null;
                unit = new ArrayAdapter<>(requireActivity(), R.layout.dialog_item);
                unit.add("Kilometer - km");
                unit.add("Meter - m");
                unit.add("Miles - mi");
                new AlertDialog.Builder(getContext())
                        .setTitle("Select Visibility Unit")
                        .setSingleChoiceItems(unit, selected[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected[0] = which;

                                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = Selected.edit();
                                editor.putInt("OptionSelectedVis", selected[0]);
                                editor.apply();
                            }
                        })
                        .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                fUnits[3] = unit.getItem(selected[0]);

                                SharedPreferences vUNIT = requireActivity().getSharedPreferences("VisibilityUnit", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = vUNIT.edit();
                                editor.putString("visUnit", fUnits[3]);
                                editor.apply();

                                uVis.setText(fUnits[3]);
                            }
                        })
                        .show();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bListener.onClicked(fUnits);
                dismiss();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] selected = {0};

                SharedPreferences Selected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = Selected.edit();
                editor.putInt("OptionSelectedVis", selected[0]);
                editor.putInt("OptionSelectedPress", selected[0]);
                editor.putInt("OptionSelectedWind", selected[0]);
                editor.apply();

                tempSwitch.setChecked(false);
                fUnits[0] = "C";
                SharedPreferences tUNIT = requireActivity().getSharedPreferences("TemperatureUnit", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorT = tUNIT.edit();
                editorT.putString("tempUnit", fUnits[0]);
                editorT.apply();

                fUnits[1] = "Kilometers per hour - km/h";
                SharedPreferences wUNIT = requireActivity().getSharedPreferences("WindUnit", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = wUNIT.edit();
                editor1.putString("windUnit", fUnits[1]);
                editor1.apply();
                uWind.setText(fUnits[1]);

                fUnits[2] = "HectoPascals - hPa";
                SharedPreferences pUNIT = requireActivity().getSharedPreferences("PressureUnit", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2 = pUNIT.edit();
                editor2.putString("pressUnit", fUnits[2]);
                editor2.apply();
                uPress.setText(fUnits[2]);

                fUnits[3] = "Kilometer - km";
                SharedPreferences vUNIT = requireActivity().getSharedPreferences("VisibilityUnit", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor3 = vUNIT.edit();
                editor3.putString("visUnit", fUnits[3]);
                editor3.apply();
                uVis.setText(fUnits[3]);

                bListener.onClicked(fUnits);
                dismiss();
            }
        });

        return v;
    }

    public interface BottomSheetListener {
        void onClicked(String[] text);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        bListener.onClicked(fUnits);
        dismiss();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            bListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }
    }


}