# Helper used by ProgrammingScreen to execute user code safely
from java.util import HashMap
import contextlib, io, traceback, textwrap


def run_user_code(code: str, expected: str):
    """Execute `code` and compare stdout to expected.
    Returns java.util.HashMap with keys ok, output, error.
    """
    code = textwrap.dedent(code)
    buf = io.StringIO()
    result = HashMap()
    try:
        with contextlib.redirect_stdout(buf):
            exec(code, {})
        output = buf.getvalue().strip()
        result.put("ok", output == expected.strip())
        result.put("output", output)
        result.put("error", "")
    except Exception:
        result.put("ok", False)
        result.put("output", buf.getvalue())
        result.put("error", traceback.format_exc(limit=1))

    return result 