import 'package:datarun_mobile/data/mobile_http_client.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const fingerprint =
      '8D6DBDC2FEFF50CCDC5DE22581095F67CA6B38B8DFE18899C214D2F0446425C5';
  const normalizedFingerprint =
      '8d6dbdc2feff50ccdc5de22581095f67ca6b38b8dfe18899c214d2f0446425c5';

  test('no explicit opt-in returns no pins', () {
    expect(debugTrustedTlsPins(), isEmpty);
  });

  test('malformed pin entries are ignored', () {
    final pins = parseDebugTrustedTlsPins(
      [
        'missing-separator',
        '=empty-host',
        'empty-fingerprint=',
        'short.example=abc123',
        'valid.example=$fingerprint',
      ].join(','),
    );

    expect(pins, {'valid.example': normalizedFingerprint});
  });

  test('host and fingerprint normalization works', () {
    final pins = parseDebugTrustedTlsPins(
      ' Keycloak.LAB = 8D:6D:BD:C2:FE:FF:50:CC:DC:5D:E2:25:81:09:5F:67:'
      'CA:6B:38:B8:DF:E1:88:99:C2:14:D2:F0:44:64:25:C5 ',
    );

    expect(pins, {'keycloak.lab': normalizedFingerprint});
  });

  test('exact host plus exact fingerprint matches', () {
    final pins = parseDebugTrustedTlsPins('keycloak.lab=$fingerprint');

    expect(
      debugTrustedTlsPinMatches(
        pins: pins,
        host: 'keycloak.lab',
        certificateSha256: normalizedFingerprint,
      ),
      isTrue,
    );
  });

  test('wrong fingerprint is rejected', () {
    final pins = parseDebugTrustedTlsPins('keycloak.lab=$fingerprint');

    expect(
      debugTrustedTlsPinMatches(
        pins: pins,
        host: 'keycloak.lab',
        certificateSha256:
            '0000000000000000000000000000000000000000000000000000000000000000',
      ),
      isFalse,
    );
  });

  test('unknown host is rejected', () {
    final pins = parseDebugTrustedTlsPins('keycloak.lab=$fingerprint');

    expect(
      debugTrustedTlsPinMatches(
        pins: pins,
        host: 'datarun-app.lab',
        certificateSha256: normalizedFingerprint,
      ),
      isFalse,
    );
  });
}
