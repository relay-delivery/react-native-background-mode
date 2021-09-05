import { NativeModules } from 'react-native';

type BackgroundModeType = {
  multiply(a: number, b: number): Promise<number>;
};

const { BackgroundMode } = NativeModules;

export default BackgroundMode as BackgroundModeType;
