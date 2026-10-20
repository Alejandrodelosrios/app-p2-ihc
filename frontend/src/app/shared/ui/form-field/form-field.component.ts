import { Component, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-form-field',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <label class="field">
      <span class="field__label">{{ label() }}</span>
      @if (options().length > 0) {
        <select
          class="field__input"
          [class.field__input--error]="error()"
          [formControl]="control()"
          [attr.aria-invalid]="!!error()"
          [attr.aria-describedby]="error() ? fieldId() + '-error' : null"
        >
          <option value="" disabled>{{ placeholder() }}</option>
          @for (option of options(); track option) {
            <option [value]="option">{{ option }}</option>
          }
        </select>
      } @else {
        <input
          class="field__input"
          [class.field__input--error]="error()"
          [type]="type()"
          [formControl]="control()"
          [attr.autocomplete]="autocomplete()"
          [attr.placeholder]="placeholder()"
          [attr.min]="min() || null"
          [attr.aria-invalid]="!!error()"
          [attr.aria-describedby]="error() ? fieldId() + '-error' : null"
        />
      }
      @if (error()) {
        <span class="field__error" [id]="fieldId() + '-error'">{{ error() }}</span>
      }
    </label>
  `,
  styleUrl: './form-field.component.css',
})
export class FormFieldComponent {
  readonly fieldId = input.required<string>();
  readonly label = input.required<string>();
  readonly control = input.required<FormControl<string>>();
  readonly type = input<'text' | 'email' | 'password' | 'date'>('text');
  readonly autocomplete = input('');
  readonly placeholder = input('');
  readonly min = input('');
  readonly options = input<readonly string[]>([]);
  readonly error = input<string | null>(null);
}
