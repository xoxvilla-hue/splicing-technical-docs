/**
 * StuckClose.gs — ปิดเส้นค้างใน Plan ด้วย "เวลาจริงเฉลี่ย" (user สั่ง 13 พ.ย. 2569: งานส่งถึงวันที่ 14 ส่งมอบหมดแล้ว)
 *   ต่างจาก _planClose_ (PlanClose.gs) ที่ไล่เวลาด้วย Std → ตัวนี้ใช้ค่ากลางเวลาจริงของเส้นที่กดครบ
 *   (ไม่นับเส้นที่เคยถูกประมาณ · เริ่มกับจบวันเดียวกัน · ข้อมูล ก.ย.–13 พ.ย. 2569)
 *   ไล่ต่อจาก "เวลาจริงล่าสุด" ของเส้นนั้นไปข้างหน้าเท่านั้น · ไม่เติมช่องที่ขาดก่อนหน้า · ไม่แตะเวลาที่กดไว้
 *   ใช้ _pcFwd_ (PlanClose.gs) ข้ามพักเที่ยง/ปิดกะ/วันอาทิตย์ · ติด t.plan_est · รันซ้ำได้ (ข้ามเส้นที่มี e_grind แล้ว)
 *   ออกจากเครื่องอบจริง → ส่งมอบ = +ACT_DELIVER ไม่เลื่อนข้ามวัน (มีคนอยู่ถึงเอาออกได้ · กติกาเดียวกับ _waitGrindClose_)
 *
 * วิธีใช้: Run previewStuckClose ดู Logger ก่อน → ถูกแล้ว Run applyStuckClose
 *   ย้อน: Version history ของชีต · หรือลบ key ตาม plan_est ใน timeData + คืนสถานะ + ลบแถว Data_Log สีส้ม
 */
var STUCK_DUE  = '2026-11-14';     // ปิดเฉพาะเส้นที่วันที่ส่ง ≤ วันนี้
var STUCK_SKIP = ['07739'];        // user กดเองจากบอร์ดหัวหน้า
var STUCK_NOTE = 'ปิดย้อนหลัง 13 พ.ย. 2569 (ไล่ด้วยเวลาจริงเฉลี่ย)';

// ค่ากลางเวลาจริง (นาที) · n = จำนวนเส้นที่ใช้คำนวณ
var ACT_SPLICE     = 46;   // ขัดหยาบส่ง → ประกบจบ (n=191)
var ACT_WAIT_BAKE  = 47;   // ประกบจบ → เข้าเครื่องอบ (n=99)
var ACT_BAKE_SMALL = 78;   // อบ 1 รอบ เครื่องเล็ก (n=122)
var ACT_BAKE_LARGE = 84;   // อบ 1 รอบ เครื่องใหญ่ (n=33)
var ACT_DELIVER    = 33;   // ออกจากเครื่องอบ → ส่งมอบ (n=161 · user เลือก 13 พ.ย. แทน 15 นาที)

function previewStuckClose() { return _stuckClose_(false); }
function applyStuckClose()   { return _stuckClose_(true); }

/** ms + mins นาที · เริ่มช่วงพักเที่ยง = เริ่ม 13:00 · คร่อมเที่ยง +1 ชม. · ไม่เลื่อนข้ามวัน */
function _stuckLunch_(ms, mins) {
  var day0 = new Date(Utilities.formatDate(new Date(ms), TZ, 'yyyy-MM-dd') + 'T00:00:00+07:00').getTime();
  var noon = day0 + 720 * 60000, back = day0 + 780 * 60000, s = ms;
  if (s >= noon && s < back) s = back;
  var e = s + mins * 60000;
  if (s < noon && e > noon) e += 60 * 60000;
  return e;
}

function _stuckClose_(apply) {
  var lock = LockService.getScriptLock();
  lock.waitLock(20000);
  try {
    var J = _jobs_(), sh = J.sheet, head = J.head;
    var cO = head.indexOf('เลขออเดอร์'), cD = head.indexOf('วันที่ส่ง'), cT = head.indexOf('timeData (JSON)'), cS = head.indexOf('สถานะ');
    var cM = head.indexOf('เครื่องอบ'), cW = head.indexOf('ความกว้าง'), cU = head.indexOf('หน่วย');
    var big = {}; _machines_().forEach(function (m) { big[m.name] = m.big; });
    var last = sh.getLastRow(), vals = sh.getRange(2, 1, last - 1, head.length).getValues();
    var now = Date.now(), out = [], n = 0;
    var hm = function (ms) { return Utilities.formatDate(new Date(ms), TZ, 'dd/MM HH:mm'); };
    out.push(apply ? '=== ปิดเส้นค้าง (ส่ง ≤ ' + STUCK_DUE + ') ===' : '=== ดูก่อน (ยังไม่เขียน) ===');
    vals.forEach(function (r, i) {
      var order = String(r[cO] || '').trim(), t = _parseTd_(r[cT]), st = String(r[cS] || '').trim();
      if (!order || t.e_grind || st === ST_DONE) return;
      var due = r[cD] instanceof Date ? Utilities.formatDate(r[cD], TZ, 'yyyy-MM-dd') : String(r[cD] || '');
      if (!due || due > STUCK_DUE) return;
      if (STUCK_SKIP.some(function (x) { return _keyOf_(x) === _keyOf_(order); })) { out.push(order + ' ข้าม (อยู่ใน STUCK_SKIP)'); return; }

      var inch = _toInch_(r[cW], r[cU]), mach = String(r[cM] || '').trim();
      var bakeMins = big[mach] ? ACT_BAKE_LARGE : ACT_BAKE_SMALL, b2 = _needBake2_(order, inch, t);
      // [key, นาทีต่อจาก key ก่อนหน้า, เครื่องอบ = เดินต่อเนื่อง]
      var seq = [['e_rough'], ['s_splice', 0], ['e_splice', ACT_SPLICE], ['s_bake', ACT_WAIT_BAKE], ['e_bake', bakeMins, true]];
      if (b2) seq.push(['s_bake2', 0, true], ['e_bake2', bakeMins, true]);
      seq.push(['s_grind', 0], ['e_grind', ACT_DELIVER]);

      // k0 = ขั้นจริงล่าสุดตามลำดับงาน · at = เวลาจริงที่ช้าที่สุด (บางเส้นกดสลับลำดับ เช่น 07732 กดประกบก่อนขัด)
      var k0 = -1, at = 0;
      seq.forEach(function (s, j) { var v = Number(t[s[0]]); if (v) { k0 = j; if (v > at) at = v; } });
      if (k0 < 0) { out.push(order + ' ข้าม — ' + st + ' · ' + (t.e_strip ? 'ไม่มีเวลาขัดหยาบส่ง' : 'ไม่มีเวลาเลย') + ' ไล่ต่อไม่ได้ (ปิดจากบอร์ด)'); return; }

      var t2 = JSON.parse(JSON.stringify(t)), est = [], atReal = true;
      for (var j = k0 + 1; j < seq.length; j++) {
        var key = seq[j][0], mins = seq[j][1], nonStop = !!seq[j][2];
        if (key === 's_grind') { t2.s_grind = at; est.push(key); continue; }     // เริ่มเจียแต่ง = ออกจากเครื่องอบ
        if (key === 'e_grind' && atReal) at = _stuckLunch_(at, mins);           // ออกจากเครื่องจริง → ไม่เลื่อนข้ามวัน
        else if (nonStop && mins && atReal) at = at + mins * 60000;              // เข้าเครื่องจริง → อบต่อเนื่อง
        else {
          var se = _pcFwd_(at, mins, nonStop), prev = seq[j - 1][0];
          if (se[0] !== at && est[est.length - 1] === prev) t2[prev] = se[0];    // ทำไม่ทันก่อนปิดกะ → ขั้นที่เราประมาณย้ายไปเริ่ม 07:30 วันถัดไป
          at = se[1];
        }
        t2[key] = at; est.push(key); atReal = false;
      }
      if (at > now) { out.push(order + ' ข้าม — ส่งมอบที่คำนวณ ' + hm(at) + ' ยังไม่ถึง'); return; }

      out.push(order + ' · ' + st + ' · ' + mach + (b2 ? ' · อบ 2 รอบ' : '') + ' · ' + est.map(function (k) { return k + ' ' + hm(t2[k]); }).join(' · '));
      n++;
      if (!apply) return;
      t2.plan_est = est.join(',') + ' · ' + STUCK_NOTE;
      sh.getRange(i + 2, cT + 1).setValue(JSON.stringify(t2));
      sh.getRange(i + 2, cS + 1).setValue(ST_DONE);
      try { _writeDataLog_(r, head, t2, at, 'หัวหน้า (ปิดย้อนหลัง)', PLAN_CLOSE_COLOR); } catch (e) { out.push('  Data_Log พัง: ' + e); }
    });
    out.push((apply ? 'ปิดแล้ว ' : 'จะปิด ') + n + ' เส้น');
    if (apply) SpreadsheetApp.flush();
    var msg = out.join('\n');
    Logger.log(msg);
    return msg;
  } finally { lock.releaseLock(); }
}
