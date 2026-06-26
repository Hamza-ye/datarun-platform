import 'package:datarun_mobile/data/mobile_http_client.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('debug TLS pins are disabled without explicit debug define', () {
    expect(debugTrustedTlsPins(), isEmpty);
  });
}
