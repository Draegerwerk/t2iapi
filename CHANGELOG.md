# Changelog
All notable changes to the t2iapi module will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- manipulation DisplayMetricWithDifferentUnit for metrics
- manipulation GetMetricDeterminationMode for metrics
- manipulation PhysicallyDisconnectRemovableSubsystemAfterPhysicalConnectorProvided
- manipulation SetActiveModeOfOperation for metrics
- manipulation PhysicallyDisconnectRemovableSubsystemAfterSettingActivationStateOnOrStndBy
- manipulation RequestIndicationOfNextCalibrationTimeRequired for devices
- manipulation IndicateTimeOfNextCalibrationToUser for devices
- manipulation GetComponentHwVersion for devices
- manipulation GetAvailableDeviceMetaData for device
- manipulation AssociateValidateAndChangeIdentificationOfPatientOrLocationContextState for contexts
- manipulation GetComponentSwVersion for devices
- manipulation ProvideInformationAboutLastCalibration for devices
- manipulation ProvideInformationAboutNextCalibration for devices
- manipulation SetSystemContextActivationStateAndContextAssociation for combined settings
- manipulation SetMdsUiLanguage for devices
- manipulation GetMdsUiSupportedLanguages for devices
- manipulation InsertContainmentTreeEntryForSequenceId for devices

## [4.1.0] - 2024-02-22

### Changed

- gRPC version to 1.60.1
- protoc version to 25.0

## [4.0.0] - 2023-11-20

### Added

- manipulation SetInvocationEffectiveTimeoutLessThanOrEqualToThreshold for operations
- manipulation SetModeOfOperationAndSetOperatingMode for combined settings
- manipulation ConveyMetricDemoValues for metrics
- manipulation SetAlertConditionAndAlertSignalActivationState for alert activation states

### Changed

- gRPC version to 1.58.0
- protoc version to 24.1

### Removed

- manipulation ChangeOperationModeStatus for metrics

## [3.0.0] - 2023-09-11

### Added

- manipulation SetComponentActivationAndSetOperatingMode for components and operations
- manipulation SetAlertActivationAndSetOperatingMode for alerts and operations
- manipulation TransitionFromCreateStateWithIdentificationToRemoveIdentification for contexts
- manipulation TransitionFromCreateStateWithIdentificationToChangeIdentification for contexts
- manipulation TransitionProvideValueToExpressNoValueAvailable for metrics
- manipulation ProvideMetricValueOrSamples for metrics
- manipulation SetSomeAlertSignalPresence for alerts
- manipulation CreateContextStateWithAssociationAndSetOperatingMode for contexts and operations
- manipulation SetBatteryUsage for device
- manipulation SetAsActivationStateOnAndChangeAcPresenceFalse for alerts
- manipulation SetActivationStateAndUserConfirmableValue for metrics
- stub files which are integrated into the python package
- description for allowed combinations of Root and Extension attributes of InstanceIdentifiers
- manipulation ComponentActivationTransition
- type hinting for python package via [types-protobuf](https://pypi.org/project/types-protobuf/)
- TriggerAnyDescriptorUpdate manipulation

### Changed

- semantics for CreateContextStateWithAssociationAndValidators manipulation
- semantics for SetMetricStatus manipulation
- semantics for the CalibrateMetric manipulation
- message SetAlarmSignalInactivationStateRequest
- semantics for the EnsembleContextIndicateMembershipWithIdentification manipulation
- semantics for the SetMetricValuesInRange manipulation
- semantics for the SetAlertConditionPresence manipulation
- semantics for SetOperatingMode manipulation
- manipulation SetInvalidValue to SetIncorrectValue, changed semantics
- semantics for SetAlertActivation, SetComponentActivation, SetAlarmSignalInactivationState, 
    SetAlertSystemNotFunctional, SetLocationDetail, RemoveAllContextStateValidators, SetContextStateAssociation,
    CreateContextStateWithAssociation, CreateContextStateWithAssocIdentificationAndValidator, 
    CreateContextStateWithAssocAndSpecificValidator, SetClockDevice, SetLanguage, SetNoValue, 
    SetMetricValuesWithQualityMode
- semantics for SetDeviceOperatingMode manipulation
- message PartialIdentification to message PartialInstanceIdentifier
- TriggerDescriptorUpdate to request an update for an arbitrary amount of handles

### Removed

- manipulation CreateContextStateWithAssociationAndUniqueIdentification, RemoveIdentificationOfContextState, 
    ChangeIdentificationOfContextState
- manipulation SetNoValue
- manipulation SetAlertSignalPresence
- manipulation ChangeMdibSequenceId
- manipulation SetUserConfirmableValue
- manipulation SetAudioPause
- manipulation CancelAudioPause
- manipulation ShutDownDevice
- manipulation BootUpDevice

## [2.0.0] - 2023-03-10

### Added

- added IsComputerControlled manipulation for metrics
- COMMON_PROTOC_VERSION=21.7 due to new versioning of protobuf

### Changed

- replaced GetRemovableDescriptors manipulation by GetRemovableDescriptorsOfClass
- JAVA_PROTOC_VERSION=3.19.2 -> JAVA_PROTOC_VERSION=3.21.7
- JAVA_GRPC_VERSION=1.45.1 -> JAVA_GRPC_VERSION=1.52.1
- PYTHON_PROTOC_VERSION=3.19.4 -> PYTHON_PROTOC_VERSION=4.21.7
- PYTHON_GRPC_VERSION=1.46.1 -> PYTHON_GRPC_VERSION=1.51.1

### Removed

- manipulation GetMetricValue
- manipulation SetMetricValuesWithQualityValidity
- manipulation GenerateElementsInStates
- manipulation SetOverflow 
- manipulation SetUnderflow 

## [1.4.0] - 2022-12-02

### Added

- added AssociatePatientWithAutoGeneratedPatientId manipulation for contexts


## [1.3.0] - 2022-11-22

### Added

- initial publicly available release
