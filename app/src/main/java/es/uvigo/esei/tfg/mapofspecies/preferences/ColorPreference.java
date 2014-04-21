package es.uvigo.esei.tfg.mapofspecies.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import es.uvigo.esei.tfg.mapofspecies.R;

/**
 * Representa una preferencia personalizada.
 * @author Alberto Pardellas Soto
 */
public class ColorPreference extends Preference {
    private int colorId;
    private View view;

    public ColorPreference(Context context) {
        super(context);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Permite obtener la vista.
     * @return vista actual.
     */
    public View getView() {
        return view;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return getContext().getResources().getIdentifier(
                a.getString(index), "drawable", getContext().getPackageName());
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            colorId = this.getPersistedInt(0);

        } else {
            colorId = (Integer) defaultValue;
            persistInt(colorId);
        }
    }

    @Override protected void onBindView(View view) {
        super.onBindView(view);

        this.view = view;
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView2);

        if (imageView != null) {
            imageView.setImageResource(colorId);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = colorId;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        colorId = myState.value;
    }

    /**
     * Permite guardar un número entero.
     * @param id número a guardar.
     */
    public void persistDrawableColorId(int id) {
        persistInt(id);
    }

    /**
     * Permite guardar y restaurar los objetos de la clase.
     */
    private static class SavedState extends BaseSavedState {
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
