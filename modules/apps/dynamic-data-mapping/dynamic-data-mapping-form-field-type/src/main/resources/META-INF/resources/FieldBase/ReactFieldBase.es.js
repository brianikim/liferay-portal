/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import './FieldBase.scss';

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import {ClayTooltipProvider} from '@clayui/tooltip';
import classNames from 'classnames';
import {
	EVENT_TYPES,
	Layout,
	getRepeatedIndex,
	useForm,
	usePage,
} from 'dynamic-data-mapping-form-renderer';
import moment from 'moment';
import React, {useMemo} from 'react';

const convertInputValue = (fieldType, value) => {
	if (fieldType === 'date') {
		const date = moment(value).toDate();

		if (moment(date).isValid()) {
			return moment(date).format('YYYY-MM-DD');
		}
	}
	else if (
		fieldType === 'document_library' ||
		fieldType === 'grid' ||
		fieldType === 'image'
	) {
		if (Object.keys(value).length === 0) {
			return '';
		}

		return JSON.stringify(value);
	}

	return value;
};

const getDefaultRows = (nestedFields) => {
	return nestedFields.map((nestedField) => {
		return {
			columns: [
				{
					fields: [nestedField],
					size: 12,
				},
			],
		};
	});
};

const FieldProperties = ({required, tooltip}) => {
	return (
		<>
			{required && (
				<span className="ddm-label-required reference-mark">
					<ClayIcon symbol="asterisk" />
				</span>
			)}

			{tooltip && (
				<span className="ddm-tooltip">
					<ClayIcon
						data-tooltip-align="right"
						symbol="question-circle-full"
						title={tooltip}
					/>
				</span>
			)}
		</>
	);
};

function FieldBase({
	children,
	displayErrors,
	errorMessage,
	id,
	label,
	localizedValue = {},
	name,
	nestedFields,
	onClick,
	overMaximumRepetitionsLimit = false,
	readOnly,
	repeatable,
	required,
	showLabel = true,
	style,
	text,
	tip,
	tooltip,
	type,
	valid,
	visible,
}) {
	const {editingLanguageId = themeDisplay.getLanguageId()} = usePage();
	let fieldDetails = '';
	const fieldDetailsId = id ? id + '_fieldDetails' : name + '_fieldDetails';
	const dispatch = useForm();
	const hasError = displayErrors && errorMessage && !valid;
	const localizedValueArray = useMemo(() => {
		const languageValues = [];

		if (!localizedValue) {
			return languageValues;
		}

		Object.keys(localizedValue).forEach((key) => {
			if (key !== editingLanguageId && localizedValue[key] !== '') {
				languageValues.push({
					name: name.replace(editingLanguageId, key),
					value: localizedValue[key],
				});
			}
		});

		return languageValues;
	}, [localizedValue, editingLanguageId, name]);

	let parentDivAriaLabelledby;
	let parentDivTabIndex;
	const renderLabel =
		(label && showLabel) || required || tooltip || repeatable;

	const repeatedIndex = useMemo(() => getRepeatedIndex(name), [name]);
	const requiredText = Liferay.Language.get('required');
	const showLegend =
		type &&
		(type === 'checkbox_multiple' ||
			type === 'grid' ||
			type === 'paragraph' ||
			type === 'radio');
	const showFor = type === 'text' || type === 'numeric';

	if (!renderLabel) {
		parentDivTabIndex = 0;
		parentDivAriaLabelledby = fieldDetailsId;
	}

	if (label) {
		fieldDetails += Liferay.Util.escape(label) + '<br>';
	}

	if (tip) {
		fieldDetails += Liferay.Util.escape(tip) + '<br>';
	}

	if (text) {
		fieldDetails +=
			(typeof text === 'object'
				? Liferay.Util.escape(text.content)
				: Liferay.Util.escape(text)) + '<br>';
	}

	if (hasError) {
		fieldDetails += Liferay.Util.escape(errorMessage);
	}
	else if (required) {
		fieldDetails += requiredText;
	}

	const accessibleProps = {
		...(fieldDetails && {'aria-labelledby': fieldDetailsId}),
		...(showFor ? {htmlFor: id ?? name} : {tabIndex: 0}),
	};

	return (
		<ClayTooltipProvider>
			<div
				aria-labelledby={parentDivAriaLabelledby}
				className={classNames('form-group', {
					'has-error': hasError,
					hide: !visible,
				})}
				data-field-name={name}
				onClick={onClick}
				style={style}
				tabIndex={parentDivTabIndex}
			>
				{repeatable && (
					<div className="lfr-ddm-form-field-repeatable-toolbar">
						{repeatable && repeatedIndex > 0 && (
							<ClayButton
								aria-label={Liferay.Util.sub(
									Liferay.Language.get(
										'remove-duplicate-field'
									),
									label ? label : type
								)}
								className="ddm-form-field-repeatable-delete-button p-0"
								disabled={readOnly}
								onClick={() =>
									dispatch({
										payload: name,
										type: EVENT_TYPES.FIELD_REMOVED,
									})
								}
								small
								title={Liferay.Language.get('remove')}
								type="button"
							>
								<ClayIcon symbol="hr" />
							</ClayButton>
						)}

						<ClayButton
							aria-label={Liferay.Util.sub(
								Liferay.Language.get('add-duplicate-field'),
								label ? label : type
							)}
							className={classNames(
								'ddm-form-field-repeatable-add-button p-0',
								{
									hide: overMaximumRepetitionsLimit,
								}
							)}
							disabled={readOnly}
							onClick={() =>
								dispatch({
									payload: name,
									type: EVENT_TYPES.FIELD_REPEATED,
								})
							}
							small
							title={Liferay.Language.get('duplicate')}
							type="button"
						>
							<ClayIcon symbol="plus" />
						</ClayButton>
					</div>
				)}

				{renderLabel && (
					<>
						{showLegend ? (
							<fieldset>
								<legend
									{...accessibleProps}
									className="lfr-ddm-legend"
								>
									{label && showLabel && label}

									<FieldProperties
										required={required}
										tooltip={tooltip}
									/>
								</legend>
								{children}
							</fieldset>
						) : (
							<>
								<label
									{...accessibleProps}
									className={classNames({
										'ddm-empty': !showLabel && !required,
										'ddm-label': showLabel || required,
									})}
								>
									{label && showLabel && label}

									<FieldProperties
										required={required}
										tooltip={tooltip}
									/>
								</label>
								{children}
							</>
						)}
					</>
				)}

				{!renderLabel && children}

				{localizedValueArray.length > 0 &&
					localizedValueArray.map((language) => (
						<input
							key={language.name}
							name={language.name}
							type="hidden"
							value={
								language.value
									? convertInputValue(type, language.value)
									: ''
							}
						/>
					))}

				{tip && (
					<span aria-hidden="true" className="form-text">
						{tip}
					</span>
				)}

				{hasError && (
					<span className="form-feedback-group">
						<div aria-hidden="true" className="form-feedback-item">
							{errorMessage}
						</div>
					</span>
				)}

				{fieldDetails && (
					<span
						className="sr-only"
						dangerouslySetInnerHTML={{
							__html: fieldDetails,
						}}
						id={fieldDetailsId}
					/>
				)}

				{nestedFields && <Layout rows={getDefaultRows(nestedFields)} />}
			</div>
		</ClayTooltipProvider>
	);
}

export {FieldBase};
