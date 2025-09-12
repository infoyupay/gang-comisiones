/**
 * Provides orchestration for concept management screens, wiring UI intents to create, edit, and list flows.<br/>
 * This package defines controllers that:
 * <ul>
 *   <li>Validate session state and propagate user privileges to the view.</li>
 *   <li>Initialize and display the management UI, binding actions to their handlers.</li>
 *   <li>Load and refresh concept lists asynchronously, handling empty and error states.</li>
 *   <li>Delegate to specialized flows for creation and editing while keeping the UI responsive.</li>
 * </ul>
 * <br/>
 * <strong>UX and reliability:</strong><br/>
 * The orchestration layer centralizes error handling and user feedback to maintain a predictable and robust user
 * experience, even when asynchronous operations fail or return empty results.
 * <br/>
 * <strong>Separation of concerns:</strong><br/>
 * Controllers focus on flow coordination and UI updates, while validation and persistence are delegated to services
 * and specialized components.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
package com.yupay.gangcomisiones.usecase.concept.manage;
