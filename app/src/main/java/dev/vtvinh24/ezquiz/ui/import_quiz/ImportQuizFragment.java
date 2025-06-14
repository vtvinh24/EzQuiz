package dev.vtvinh24.ezquiz.ui.import_quiz;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.domain.ImportManager;
import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;

/**
 * Fragment for importing quizzes from external sources.
 */
public class ImportQuizFragment extends Fragment {

    private ImportManager importManager;
    private ActivityResultLauncher<String[]> filePickerLauncher;
    private RadioGroup formatRadioGroup;
    private Button selectFileButton;
    private TextView selectedFileTextView;
    private Button importButton;
    private Uri selectedFileUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize import manager
        // Note: In a real app, this would be injected with Hilt
        importManager = new ImportManager(requireContext(), new InMemoryQuizRepository());

        // Initialize file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), uri -> {
                    if (uri != null) {
                        selectedFileUri = uri;
                        selectedFileTextView.setText(uri.getLastPathSegment());
                        importButton.setEnabled(true);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_import_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        formatRadioGroup = view.findViewById(R.id.format_radio_group);
        selectFileButton = view.findViewById(R.id.select_file_button);
        selectedFileTextView = view.findViewById(R.id.selected_file_text);
        importButton = view.findViewById(R.id.import_button);

        // Setup initial state
        importButton.setEnabled(false);

        // Setup click listeners
        selectFileButton.setOnClickListener(v -> openFilePicker());
        importButton.setOnClickListener(v -> importSelectedFile());
    }

    private void openFilePicker() {
        // Launch file picker for CSV and JSON files
        filePickerLauncher.launch(new String[] {"text/csv", "application/json"});
    }

    private void importSelectedFile() {
        if (selectedFileUri == null) {
            Toast.makeText(requireContext(), "Please select a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine selected format
        int selectedRadioButtonId = formatRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = formatRadioGroup.findViewById(selectedRadioButtonId);

        if (selectedRadioButton == null) {
            Toast.makeText(requireContext(), "Please select a format", Toast.LENGTH_SHORT).show();
            return;
        }

        ImportManager.ImportFormat format;
        String formatText = selectedRadioButton.getText().toString();
        if (formatText.equals("CSV")) {
            format = ImportManager.ImportFormat.CSV;
        } else if (formatText.equals("JSON")) {
            format = ImportManager.ImportFormat.JSON;
        } else {
            Toast.makeText(requireContext(), "Invalid format selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to import the quiz
        Quiz importedQuiz = importManager.importQuiz(selectedFileUri, format);

        if (importedQuiz != null) {
            Toast.makeText(requireContext(), "Quiz imported successfully", Toast.LENGTH_SHORT).show();
            // Navigate back to collections
            Navigation.findNavController(requireView())
                     .navigate(R.id.action_import_to_collections);
        } else {
            Toast.makeText(requireContext(), "Failed to import quiz", Toast.LENGTH_SHORT).show();
        }
    }
}
