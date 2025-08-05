# Helper used by ProgrammingScreen to execute user code safely
from java.util import HashMap
import contextlib, io, traceback, textwrap, sys


def run_user_code(code: str, expected: str, stdin_data: str = ""):
    """Execute `code` with optional stdin and compare stdout to expected.
    Returns java.util.HashMap with keys ok, output, error.
    """
    code = textwrap.dedent(code)
    buf_out = io.StringIO()
    buf_in = io.StringIO(stdin_data)
    result = HashMap()
    orig_stdin = sys.stdin
    try:
        sys.stdin = buf_in
        with contextlib.redirect_stdout(buf_out):
            exec(code, {})
        output = buf_out.getvalue().strip()
        exp = expected.strip()
        out_clean = output.strip()
        last_line = next((l for l in reversed(out_clean.splitlines()) if l.strip()), "")
        ok = False
        if out_clean == exp or last_line == exp:
            ok = True
        elif last_line.endswith(exp):
            ok = True
        result.put("ok", ok)
        result.put("output", output)
        result.put("error", "")
    except Exception:
        result.put("ok", False)
        result.put("output", buf_out.getvalue())
        result.put("error", traceback.format_exc(limit=1))
    finally:
        sys.stdin = orig_stdin

    return result 